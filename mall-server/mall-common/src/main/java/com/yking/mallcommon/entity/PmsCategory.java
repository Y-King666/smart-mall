package com.yking.mallcommon.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 商品分类实体类
 *
 * 对应数据库表：pms_category
 * 支持树形层级结构（通过 parentId 实现父子关系）
 *
 * 字段说明：
 * - id：主键，自增
 * - name：分类名称
 * - parentId：父分类 ID（0 表示顶级分类）
 * - sort：排序权重（值越小越靠前）
 * - icon：图标名称（对应 Element Plus 图标组件名，如 "Reading"、"Monitor"）
 * - deleted：逻辑删除标记
 * - createTime：创建时间
 *
 * 层级示例：
 *   数字商品（id=1, parentId=0）
 *     ├── 在线课程（id=2, parentId=1）
 *     └── 软件工具（id=3, parentId=1）
 */
@TableName(value ="pms_category")
@Data
public class PmsCategory {

    /** 主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类 ID（0 表示顶级分类） */
    private Long parentId;

    /** 排序权重（值越小越靠前） */
    private Integer sort;

    /** 图标名称（Element Plus 图标组件名） */
    private String icon;

    /** 逻辑删除标记：0=未删除，1=已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 子分类列表（非数据库字段）
     *
     * 仅在返回分类树时填充，用于承载多级嵌套结构。
     * exist = false 告知 MyBatis-Plus 该字段不参与 SQL 映射。
     */
    @TableField(exist = false)
    private List<PmsCategory> children;
}