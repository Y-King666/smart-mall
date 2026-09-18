package com.yking.malladmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yking.malladmin.dto.OrderDetailVO;
import com.yking.malladmin.dto.OrderStatsVO;
import com.yking.malladmin.dto.OrderVO;
import com.yking.malladmin.service.OrderService;
import com.yking.mallcommon.entity.OmsOrder;
import com.yking.mallcommon.entity.OmsOrderItem;
import com.yking.mallcommon.exception.BusinessException;
import com.yking.mallcommon.mapper.OmsOrderItemMapper;
import com.yking.mallcommon.mapper.OmsOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    /** 支付状态：待支付 */
    private static final int PAY_STATUS_PENDING = 0;

    /** 支付状态：已支付 */
    private static final int PAY_STATUS_PAID = 1;

    /** 支付状态：已取消 */
    private static final int PAY_STATUS_CANCELLED = 2;

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;

    public OrderServiceImpl(OmsOrderMapper omsOrderMapper, OmsOrderItemMapper omsOrderItemMapper) {
        this.omsOrderMapper = omsOrderMapper;
        this.omsOrderItemMapper = omsOrderItemMapper;
    }

    @Override
    public IPage<OrderVO> page(long page, long size, String orderNo, Integer payStatus) {
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<OmsOrder>()
                .like(StringUtils.hasText(orderNo), OmsOrder::getOrderNo, orderNo)
                .eq(payStatus != null, OmsOrder::getPayStatus, payStatus)
                // 按 id 倒序，保证分页结果稳定
                .orderByDesc(OmsOrder::getId);
        return omsOrderMapper.selectPage(new Page<>(page, size), wrapper).convert(OrderVO::from);
    }

    @Override
    public OrderDetailVO detail(Long id) {
        OmsOrder order = omsOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return OrderDetailVO.from(order, listOrderItems(id));
    }

    @Override
    public OrderStatsVO stats() {
        OrderStatsVO stats = new OrderStatsVO();
        stats.setTotalCount(omsOrderMapper.selectCount(null));
        stats.setPendingCount(countByPayStatus(PAY_STATUS_PENDING));
        stats.setPaidCount(countByPayStatus(PAY_STATUS_PAID));
        stats.setCancelledCount(countByPayStatus(PAY_STATUS_CANCELLED));
        stats.setTotalAmount(sumAmount());
        return stats;
    }

    /** 查询订单的商品明细 */
    private List<OmsOrderItem> listOrderItems(Long orderId) {
        return omsOrderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
    }

    private Long countByPayStatus(int payStatus) {
        return omsOrderMapper.selectCount(
                new LambdaQueryWrapper<OmsOrder>().eq(OmsOrder::getPayStatus, payStatus));
    }

    /**
     * 汇总订单金额
     *
     * 只统计「已支付」订单，与仪表盘、mall-analytics 口径一致：
     * 待支付尚未成交、已取消（含支付后取消）要退款，都不算销售额。
     */
    private BigDecimal sumAmount() {
        QueryWrapper<OmsOrder> wrapper = new QueryWrapper<>();
        wrapper.select("COALESCE(SUM(total_amount), 0) AS total")
                .eq("pay_status", PAY_STATUS_PAID);
        Map<String, Object> row = omsOrderMapper.selectMaps(wrapper).get(0);
        return new BigDecimal(row.get("total").toString());
    }
}
