package com.yking.mallportal.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车行数据
 *
 * 购物车表只存商品 ID 与数量，商品信息在查询时关联商品表带出，
 * 使前端一次请求即可渲染完整的购物车列表。
 */
@Data
public class CartItemVO {

    /** 购物车项 ID */
    private Long id;

    /** 商品 ID */
    private Long productId;

    /** 商品名称 */
    private String name;

    /** 商品封面图 URL */
    private String coverImage;

    /** 商品单价 */
    private BigDecimal price;

    /** 商品当前库存 */
    private Integer stock;

    /** 商品上架状态：1=上架，0=下架（下架商品不允许结算） */
    private Integer status;

    /** 购买数量 */
    private Integer quantity;

    /** 选中状态：1=已选中，0=未选中 */
    private Integer checked;
}
