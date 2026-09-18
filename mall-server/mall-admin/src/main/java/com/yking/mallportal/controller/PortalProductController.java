package com.yking.mallportal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yking.mallportal.service.PortalProductService;
import com.yking.malladmin.service.CategoryService;
import com.yking.mallcommon.Result;
import com.yking.mallcommon.entity.PmsCategory;
import com.yking.mallcommon.entity.PmsProduct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端商品浏览接口
 *
 * 按 README 的划分，商品列表、商品详情、分类树都归在本控制器下，
 * 因此类级映射到 /api/portal，再由方法各自声明子路径。
 * 这些接口在 SecurityConfig 中已配置为公开访问，无需登录。
 */
@RestController
@RequestMapping("/api/portal")
public class PortalProductController {

    private final PortalProductService portalProductService;
    private final CategoryService categoryService;

    public PortalProductController(PortalProductService portalProductService, CategoryService categoryService) {
        this.portalProductService = portalProductService;
        this.categoryService = categoryService;
    }

    /**
     * 商品列表（分页，只返回上架商品）
     *
     * keyword、categoryId、sort 均可选，为空时不过滤、按上架时间倒序。
     * sort=sales 时按销量倒序，供首页热销榜使用。
     */
    @GetMapping("/products")
    public Result<IPage<PmsProduct>> list(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "12") long size,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long categoryId,
                                         @RequestParam(required = false) String sort) {
        return Result.success(portalProductService.page(page, size, keyword, categoryId, sort));
    }

    /** 商品详情（下架商品按不存在处理） */
    @GetMapping("/products/{id}")
    public Result<PmsProduct> detail(@PathVariable Long id) {
        return Result.success(portalProductService.getById(id));
    }

    /** 分类树，与管理端共用同一份实现 */
    @GetMapping("/categories")
    public Result<List<PmsCategory>> categories() {
        return Result.success(categoryService.getTree());
    }
}
