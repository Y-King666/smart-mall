package com.yking.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yking.malladmin.dto.OrderDetailVO;
import com.yking.mallportal.dto.DirectOrderRequest;
import com.yking.mallportal.service.PortalOrderService;
import com.yking.mallcommon.entity.OmsCartItem;
import com.yking.mallcommon.entity.OmsOrder;
import com.yking.mallcommon.entity.OmsOrderItem;
import com.yking.mallcommon.entity.PmsProduct;
import com.yking.mallcommon.exception.BusinessException;
import com.yking.mallcommon.mapper.OmsCartItemMapper;
import com.yking.mallcommon.mapper.OmsOrderItemMapper;
import com.yking.mallcommon.mapper.OmsOrderMapper;
import com.yking.mallcommon.mapper.PmsProductMapper;
import com.yking.mallcommon.service.OrderCancelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class PortalOrderServiceImpl implements PortalOrderService {

    /** 支付状态：待支付 */
    private static final int PAY_STATUS_PENDING = 0;

    /** 支付状态：已支付 */
    private static final int PAY_STATUS_PAID = 1;

    /** 商品上架状态 */
    private static final int STATUS_ON_SALE = 1;

    /** 购物车项已选中 */
    private static final int CHECKED = 1;

    /** 订单号中时间部分的格式 */
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final OmsCartItemMapper omsCartItemMapper;
    private final PmsProductMapper pmsProductMapper;
    private final OrderCancelService orderCancelService;

    public PortalOrderServiceImpl(OmsOrderMapper omsOrderMapper,
                                  OmsOrderItemMapper omsOrderItemMapper,
                                  OmsCartItemMapper omsCartItemMapper,
                                  PmsProductMapper pmsProductMapper,
                                  OrderCancelService orderCancelService) {
        this.omsOrderMapper = omsOrderMapper;
        this.omsOrderItemMapper = omsOrderItemMapper;
        this.omsCartItemMapper = omsCartItemMapper;
        this.pmsProductMapper = pmsProductMapper;
        this.orderCancelService = orderCancelService;
    }

    /**
     * 提交订单
     *
     * 扣库存、写订单、写明细、清购物车必须整体成功或整体失败，
     * 任一步抛异常都会回滚，不会出现「扣了库存却没有订单」的中间状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long userId) {
        // 1. 取出购物车中已选中的商品
        List<OmsCartItem> checkedItems = omsCartItemMapper.selectList(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getUserId, userId)
                        .eq(OmsCartItem::getChecked, CHECKED));
        if (checkedItems.isEmpty()) {
            throw new BusinessException("请先在购物车中选中要购买的商品");
        }

        // 2. 逐项校验并扣减库存，同时计算订单总额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OmsOrderItem> orderItems = new ArrayList<>(checkedItems.size());
        for (OmsCartItem cartItem : checkedItems) {
            PmsProduct product = pmsProductMapper.selectById(cartItem.getProductId());
            if (product == null || product.getStatus() != STATUS_ON_SALE) {
                throw new BusinessException("购物车中有商品已下架，请移除后再结算");
            }

            int quantity = cartItem.getQuantity();
            orderItems.add(deductStock(product, quantity));
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        // 3. 写订单与明细
        createOrder(userId, orderItems, totalAmount);

        // 4. 清除已下单的购物车项
        omsCartItemMapper.delete(new LambdaQueryWrapper<OmsCartItem>()
                .eq(OmsCartItem::getUserId, userId)
                .eq(OmsCartItem::getChecked, CHECKED));
    }

    /**
     * 直接下单并立即支付（商品详情页的「立即购买」）
     *
     * 不经过购物车：只买这一件，也不改动用户购物车里的任何内容，
     * 因此把「校验商品 → 扣库存 → 写订单 → 支付」串成一次事务。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitDirect(Long userId, DirectOrderRequest request) {
        PmsProduct product = pmsProductMapper.selectById(request.getProductId());
        if (product == null || product.getStatus() != STATUS_ON_SALE) {
            throw new BusinessException("商品不存在或已下架");
        }

        int quantity = request.getQuantity();
        OmsOrderItem orderItem = deductStock(product, quantity);
        OmsOrder order = createOrder(userId, List.of(orderItem),
                product.getPrice().multiply(BigDecimal.valueOf(quantity)));

        // 下单即支付：复用支付逻辑，销量累加等规则都在里面
        pay(userId, order.getId());
        return order.getOrderNo();
    }

    /**
     * 扣减库存并生成下单时的商品快照
     *
     * 扣减与判断合并成一条 SQL（只有 stock >= quantity 才会更新）：
     * 库存不足时影响行数为 0，避免「先查询再更新」在并发下超卖。
     * quantity 是取回来的整数，拼接无注入风险。
     */
    private OmsOrderItem deductStock(PmsProduct product, int quantity) {
        int rows = pmsProductMapper.update(null, new LambdaUpdateWrapper<PmsProduct>()
                .eq(PmsProduct::getId, product.getId())
                .ge(PmsProduct::getStock, quantity)
                .setSql("stock = stock - " + quantity));
        if (rows == 0) {
            throw new BusinessException("商品「" + product.getName() + "」库存不足");
        }

        // 明细保存下单时的名称与单价快照，商品日后改名或调价都不影响历史订单
        OmsOrderItem orderItem = new OmsOrderItem();
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setProductPrice(product.getPrice());
        orderItem.setQuantity(quantity);
        return orderItem;
    }

    /** 写订单及其明细，订单号与自增主键都在这里生成 */
    private OmsOrder createOrder(Long userId, List<OmsOrderItem> orderItems, BigDecimal totalAmount) {
        OmsOrder order = new OmsOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayStatus(PAY_STATUS_PENDING);
        omsOrderMapper.insert(order);

        for (OmsOrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            omsOrderItemMapper.insert(orderItem);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long userId, Long orderId) {
        getOwnedOrder(userId, orderId);

        // 把「状态必须是待支付」写进更新条件，并发重复支付时只有一次能成功，
        // 避免商品销量被重复累加
        int rows = omsOrderMapper.update(null, new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .eq(OmsOrder::getPayStatus, PAY_STATUS_PENDING)
                .set(OmsOrder::getPayStatus, PAY_STATUS_PAID)
                .set(OmsOrder::getPayTime, LocalDateTime.now()));
        if (rows == 0) {
            throw new BusinessException("该订单当前状态不可支付");
        }

        // 支付成功后累加商品销量
        for (OmsOrderItem orderItem : listOrderItems(orderId)) {
            pmsProductMapper.update(null, new LambdaUpdateWrapper<PmsProduct>()
                    .eq(PmsProduct::getId, orderItem.getProductId())
                    .setSql("sales_count = sales_count + " + orderItem.getQuantity()));
        }
    }

    /**
     * 我的订单列表
     *
     * 列表上要直接看出每笔订单买了什么，所以每笔都带上商品明细。
     * 明细里的封面图与简介不落库，这里按一页订单批量补齐，避免逐条查商品表。
     */
    @Override
    public IPage<OrderDetailVO> page(Long userId, long page, long size) {
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getUserId, userId)
                .orderByDesc(OmsOrder::getId);
        IPage<OmsOrder> orderPage = omsOrderMapper.selectPage(new Page<>(page, size), wrapper);
        if (orderPage.getRecords().isEmpty()) {
            return orderPage.convert(order -> OrderDetailVO.from(order, List.of()));
        }

        // 1. 一次取出这一页订单的全部明细
        List<Long> orderIds = orderPage.getRecords().stream().map(OmsOrder::getId).toList();
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().in(OmsOrderItem::getOrderId, orderIds));

        // 2. 一次取出明细涉及的商品，用于补上封面图与简介
        List<Long> productIds = items.stream().map(OmsOrderItem::getProductId).distinct().toList();
        Map<Long, PmsProduct> productMap = productIds.isEmpty() ? Map.of()
                : pmsProductMapper.selectBatchIds(productIds).stream()
                        .collect(Collectors.toMap(PmsProduct::getId, product -> product));

        // 3. 按订单归拢明细；商品已被删除时明细照旧保留（名称与价格是下单时的快照），只是没有图与简介
        Map<Long, List<OmsOrderItem>> itemsByOrder = new HashMap<>();
        for (OmsOrderItem item : items) {
            PmsProduct product = productMap.get(item.getProductId());
            if (product != null) {
                item.setCoverImage(product.getCoverImage());
                item.setDescription(product.getDescription());
            }
            itemsByOrder.computeIfAbsent(item.getOrderId(), key -> new ArrayList<>()).add(item);
        }

        return orderPage.convert(order ->
                OrderDetailVO.from(order, itemsByOrder.getOrDefault(order.getId(), List.of())));
    }

    /**
     * 取消订单
     *
     * 取消规则（校验归属、恢复库存、按需扣回销量）放在公共模块，
     * 与 AI 客服的「取消订单」工具共用同一份，避免两处各写一遍导致数据对不上。
     */
    @Override
    public void cancel(Long userId, Long orderId) {
        orderCancelService.cancel(userId, orderId);
    }

    /**
     * 查询订单并校验归属
     *
     * 「订单不存在」与「订单不属于当前用户」返回同一条提示，
     * 避免通过提示差异探测他人订单是否存在。
     */
    private OmsOrder getOwnedOrder(Long userId, Long orderId) {
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    /** 查询订单的商品明细 */
    private List<OmsOrderItem> listOrderItems(Long orderId) {
        return omsOrderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
    }

    /** 生成订单号：14 位时间 + 6 位随机数，与库中现有订单号的格式保持一致 */
    private String generateOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_FORMATTER)
                + ThreadLocalRandom.current().nextInt(100000, 1000000);
    }
}
