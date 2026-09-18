package com.yking.mallportal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入购物车请求参数
 */
@Data
public class CartAddRequest {

    @NotNull(message = "请选择商品")
    private Long productId;

    @NotNull(message = "请输入购买数量")
    @Min(value = 1, message = "购买数量不能小于 1")
    private Integer quantity;
}
