package com.yking.mallcommon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 订单商品项实体类
 *
 * 对应数据库表：oms_order_item
 * 存储订单中的每个商品明细（一个订单可包含多个商品项）
 *
 * 字段说明：
 * - id：主键，自增
 * - orderId：所属订单 ID（关联 oms_order.id）
 * - productId：商品 ID（关联 pms_product.id）
 * - productName：商品名称（下单时快照，避免商品改名影响历史订单）
 * - productPrice：商品单价（下单时快照）
 * - quantity：购买数量
 *
 * 注意：订单项中的 productName 和 productPrice 是下单时的快照值，
 * 不随商品表更新而变化，保证历史订单数据的准确性。
 * 封面图与简介不做快照、也不落库，由查询时从 pms_product 关联填充（见下面两个非表字段）。
 */
@TableName(value ="oms_order_item")
@Data
public class OmsOrderItem {

    /** 主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属订单 ID（关联 oms_order.id） */
    private Long orderId;

    /** 商品 ID（关联 pms_product.id） */
    private Long productId;

    /** 商品名称（下单时快照） */
    private String productName;

    /** 商品单价（下单时快照） */
    private BigDecimal productPrice;

    /** 购买数量 */
    private Integer quantity;

    /** 商品封面图（非表字段，查询时从 pms_product 关联填充） */
    @TableField(exist = false)
    private String coverImage;

    /** 商品简介（非表字段，查询时从 pms_product 关联填充） */
    @TableField(exist = false)
    private String description;
}