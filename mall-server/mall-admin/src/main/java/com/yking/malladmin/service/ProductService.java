package com.yking.malladmin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yking.malladmin.dto.ProductForm;
import com.yking.mallcommon.entity.PmsProduct;

/**
 * 商品管理业务逻辑
 */
public interface ProductService {

    /**
     * 分页查询商品
     *
     * @param keyword    商品名称模糊关键字，为空则不过滤
     * @param categoryId 分类 ID，为 null 则不过滤
     * @param status     上架状态，为 null 则不过滤
     */
    IPage<PmsProduct> page(long page, long size, String keyword, Long categoryId, Integer status);

    /** 查询商品详情 */
    PmsProduct getById(Long id);

    /** 新增商品 */
    void create(ProductForm form);

    /** 编辑商品 */
    void update(Long id, ProductForm form);

    /** 切换上下架状态 */
    void toggleStatus(Long id);

    /** 删除商品（逻辑删除） */
    void delete(Long id);
}
