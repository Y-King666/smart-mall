package com.yking.malladmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品新增/编辑表单参数
 *
 * 字段与前端商品编辑页提交的表单一一对应（共 9 个字段，全量提交）。
 */
@Data
public class ProductForm {

    @NotBlank(message = "请输入商品名称")
    private String name;

    /** 所属分类 ID */
    private Long categoryId;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    /** 原价（划线价） */
    private BigDecimal originalPrice;

    /** 库存数量 */
    private Integer stock;

    /** 封面图 URL */
    private String coverImage;

    /** 商品简要描述 */
    private String description;

    /** 商品详情（HTML 富文本） */
    private String detail;

    /** 上架状态：1=上架，0=下架 */
    private Integer status;
}
