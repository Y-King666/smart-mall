package com.yking.malladmin.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单统计汇总数据
 */
@Data
public class OrderStatsVO {

    /** 订单总数 */
    private Long totalCount;

    /** 待支付订单数（payStatus=0） */
    private Long pendingCount;

    /** 已支付订单数（payStatus=1） */
    private Long paidCount;

    /** 已取消订单数（payStatus=2） */
    private Long cancelledCount;

    /** 订单总金额 */
    private BigDecimal totalAmount;
}
