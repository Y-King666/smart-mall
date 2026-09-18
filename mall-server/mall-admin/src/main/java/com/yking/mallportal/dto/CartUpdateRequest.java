package com.yking.mallportal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改购物车项请求参数
 *
 * 数量与选中状态共用一个更新接口（与 README 列出的接口清单一致），
 * 两者都可为空，为空表示该项保持不变。
 */
@Data
public class CartUpdateRequest {

    @NotNull(message = "缺少购物车项 ID")
    private Long id;

    /** 购买数量，为空表示不修改 */
    @Min(value = 1, message = "购买数量不能小于 1")
    private Integer quantity;

    /** 选中状态：1=已选中，0=未选中，为空表示不修改 */
    private Integer checked;
}
