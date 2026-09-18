package com.yking.malladmin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类新增/编辑表单参数
 *
 * 编辑时前端不传 id（id 通过路径变量传递）。
 */
@Data
public class CategoryForm {

    @NotBlank(message = "请输入分类名称")
    private String name;

    /** 父分类 ID，0 表示顶级分类 */
    private Long parentId;

    /** 排序权重，值越小越靠前 */
    private Integer sort;
}
