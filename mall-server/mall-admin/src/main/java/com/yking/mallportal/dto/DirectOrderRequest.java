package com.yking.mallportal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 直接下单请求
 *
 * 用于商品详情页的「立即购买」：只买这一件商品，不经过购物车。
 */
@Data
public class DirectOrderRequest {

    /** 要购买的商品 ID */
    @NotNull(message = "请选择商品")
    private Long productId;

    /** 购买数量 */
    @NotNull(message = "请选择购买数量")
    @Min(value = 1, message = "购买数量至少为 1")
    private Integer quantity;
}
