package com.yking.mallcommon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yking.mallcommon.entity.OmsOrder;
import com.yking.mallcommon.entity.OmsOrderItem;
import com.yking.mallcommon.entity.PmsProduct;
import com.yking.mallcommon.exception.BusinessException;
import com.yking.mallcommon.mapper.OmsOrderItemMapper;
import com.yking.mallcommon.mapper.OmsOrderMapper;
import com.yking.mallcommon.mapper.PmsProductMapper;
import com.yking.mallcommon.service.OrderCancelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderCancelServiceImpl implements OrderCancelService {

    /** 支付状态：已支付 */
    private static final int PAY_STATUS_PAID = 1;

    /** 支付状态：已取消 */
    private static final int PAY_STATUS_CANCELLED = 2;

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final PmsProductMapper pmsProductMapper;

    public OrderCancelServiceImpl(OmsOrderMapper omsOrderMapper,
                                  OmsOrderItemMapper omsOrderItemMapper,
                                  PmsProductMapper pmsProductMapper) {
        this.omsOrderMapper = omsOrderMapper;
        this.omsOrderItemMapper = omsOrderItemMapper;
        this.pmsProductMapper = pmsProductMapper;
    }

    @Override
    public OmsOrder findByOrderNo(Long userId, String orderNo) {
        return omsOrderMapper.selectOne(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getUserId, userId)
                .eq(OmsOrder::getOrderNo, orderNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long orderId) {
        OmsOrder order = getOwnedOrder(userId, orderId);

        // 已取消的订单不再重复处理，否则库存会被重复恢复。
        // 把「状态不是已取消」写进更新条件，并发重复取消时只有一次能生效
        int rows = omsOrderMapper.update(null, new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, orderId)
                .ne(OmsOrder::getPayStatus, PAY_STATUS_CANCELLED)
                .set(OmsOrder::getPayStatus, PAY_STATUS_CANCELLED));
        if (rows == 0) {
            throw new BusinessException("订单已取消，请勿重复操作");
        }

        // 已支付订单在支付时累加过销量，取消时要扣回
        boolean wasPaid = order.getPayStatus() == PAY_STATUS_PAID;
        List<OmsOrderItem> orderItems = omsOrderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        for (OmsOrderItem orderItem : orderItems) {
            int quantity = orderItem.getQuantity();

            // quantity 是库里的整数，拼接无注入风险
            pmsProductMapper.update(null, new LambdaUpdateWrapper<PmsProduct>()
                    .eq(PmsProduct::getId, orderItem.getProductId())
                    .setSql("stock = stock + " + quantity));

            if (wasPaid) {
                // GREATEST 兜底，避免销量被扣成负数
                pmsProductMapper.update(null, new LambdaUpdateWrapper<PmsProduct>()
                        .eq(PmsProduct::getId, orderItem.getProductId())
                        .setSql("sales_count = GREATEST(sales_count - " + quantity + ", 0)"));
            }
        }
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
}
