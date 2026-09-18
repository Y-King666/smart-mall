package com.yking.mallcommon.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 商品实体类
 *
 * 对应数据库表：pms_product（Product Management System - 商品管理系统）
 * 存储所有商品的基本信息，包括虚拟商品（课程、会员、软件等）
 *
 * 字段说明：
 * - id：主键，自增
 * - name：商品名称
 * - categoryId：所属分类 ID（关联 pms_category.id，支持父子分类）
 * - price：当前售价
 * - originalPrice：原价（用于显示划线价）
 * - stock：库存数量
 * - coverImage：封面图 URL（本地上传路径如 /api/uploads/images/...）
 * - description：商品简要描述（列表页展示）
 * - detail：商品详情（支持 HTML 富文本，详情页展示）
 * - status：上架状态（1=上架，0=下架）
 * - salesCount：累计销量
 * - deleted：逻辑删除标记
 * - createTime/updateTime：时间戳（自动填充）
 */
@TableName(value ="pms_product")
@Data
public class PmsProduct {

    /** 主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 所属分类 ID（关联 pms_category.id） */
    private Long categoryId;

    /** 当前售价 */
    private BigDecimal price;

    /** 原价（划线价） */
    private BigDecimal originalPrice;

    /** 库存数量 */
    private Integer stock;

    /** 封面图 URL（本地上传路径或网络地址） */
    private String coverImage;

    /** 商品简要描述 */
    private String description;

    /** 商品详情（HTML 富文本） */
    private String detail;

    /** 上架状态：1=上架，0=下架 */
    private Integer status;

    /** 累计销量 */
    private Integer salesCount;

    /** 逻辑删除标记：0=未删除，1=已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}