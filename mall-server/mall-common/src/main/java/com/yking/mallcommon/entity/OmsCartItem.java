package com.yking.mallcommon.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;


/**
 * 购物车项实体类
 *
 * 对应数据库表：oms_cart_item（Order Management System - 购物车）
 * 每条记录代表购物车中的一个商品项
 *
 * 字段说明：
 * - id：主键，自增
 * - userId：所属用户 ID（关联 sys_user.id）
 * - productId：商品 ID（关联 pms_product.id）
 * - quantity：购买数量
 * - checked：选中状态（1=已选中，0=未选中），用于结算时筛选
 * - createTime：加入购物车的时间
 */
@TableName(value ="oms_cart_item")
@Data
public class OmsCartItem {

    /** 主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 商品 ID（关联 pms_product.id） */
    private Long productId;

    /** 购买数量 */
    private Integer quantity;

    /** 选中状态：1=已选中，0=未选中（结算时只计算选中的商品） */
    private Integer checked;

    /** 加入购物车的时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}