package com.yking.malladmin.dto;

import com.yking.mallcommon.entity.OmsOrder;
import com.yking.mallcommon.util.DateTimeUtil;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单列表行数据
 *
 * 下单时间/支付时间按 yyyy-MM-dd HH:mm:ss 输出为字符串，
 * 前端表格直接展示、不做格式化。管理端与用户端共用本结构。
 */
@Data
public class OrderVO {

    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 下单用户 ID */
    private Long userId;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 支付状态：0=待支付，1=已支付，2=已取消 */
    private Integer payStatus;

    /** 支付时间，未支付时返回空串 */
    private String payTime;

    /** 下单时间，格式 yyyy-MM-dd HH:mm:ss */
    private String createTime;

    /** 由订单实体构建列表行数据 */
    public static OrderVO from(OmsOrder order) {
        return copy(order, new OrderVO());
    }

    /**
     * 把订单实体的字段写入目标 VO
     *
     * @param vo 目标对象，可为 OrderVO 或其子类 OrderDetailVO
     */
    protected static <T extends OrderVO> T copy(OmsOrder order, T vo) {
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayStatus(order.getPayStatus());
        vo.setPayTime(DateTimeUtil.format(order.getPayTime()));
        vo.setCreateTime(DateTimeUtil.format(order.getCreateTime()));
        return vo;
    }
}
