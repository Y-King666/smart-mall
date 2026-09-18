package com.yking.mallportal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yking.mallcommon.entity.PmsProduct;

/**
 * 用户端商品浏览业务逻辑
 */
public interface PortalProductService {

    /**
     * 分页查询在售商品
     *
     * @param keyword    商品名称模糊关键字，为空则不过滤
     * @param categoryId 分类 ID，为 null 则不过滤
     * @param sort       排序方式，sales=按销量倒序（热销榜），其它值或不传=按上架时间倒序
     */
    IPage<PmsProduct> page(long page, long size, String keyword, Long categoryId, String sort);

    /** 查询商品详情（下架商品按不存在处理） */
    PmsProduct getById(Long id);
}
