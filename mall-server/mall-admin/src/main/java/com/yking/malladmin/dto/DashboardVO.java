package com.yking.malladmin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表盘统计数据
 *
 * 前端对 totalSales / todaySales 调用 toFixed(2)，因此这两个字段必须是数值类型。
 */
@Data
public class DashboardVO {

    /** 商品总数 */
    private Long productCount;

    /** 订单总数 */
    private Long orderCount;

    /** 总销售额 */
    private BigDecimal totalSales;

    /** 今日销售额 */
    private BigDecimal todaySales;

    /** 近 7 天销售趋势，含无订单的日期（补 0），保证折线图横轴连续 */
    private List<DailySales> recent7Days;

    /**
     * 单日销售数据
     *
     * @param date   日期，格式 MM-dd
     * @param sales  当日销售额
     * @param orders 当日订单数
     */
    public record DailySales(String date, BigDecimal sales, Long orders) {
    }
}
