package com.yking.mallcommon.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 订单实体类
 *
 * 对应数据库表：oms_order（Order Management System - 订单）
 * 存储订单的基本信息，商品明细存储在 oms_order_item 表中
 *
 * 字段说明：
 * - id：主键，自增
 * - orderNo：订单编号（唯一，用于前端展示和查询）
 * - userId：下单用户 ID（关联 sys_user.id）
 * - totalAmount：订单总金额（所有商品项的 price × quantity 之和）
 * - payStatus：支付状态（0=待支付，1=已支付，2=已取消）
 * - payTime：支付时间（未支付时为 null）
 * - deleted：逻辑删除标记
 * - createTime：下单时间
 * - updateTime：最后更新时间
 */
@TableName(value ="oms_order")
@Data
public class OmsOrder {

    /** 主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号（唯一） */
    private String orderNo;

    /** 下单用户 ID */
    private Long userId;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 支付状态：0=待支付，1=已支付，2=已取消 */
    private Integer payStatus;

    /** 支付时间（未支付时为 null） */
    private LocalDateTime payTime;

    /** 逻辑删除标记：0=未删除，1=已删除 */
    @TableLogic
    private Integer deleted;

    /** 下单时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最后更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}