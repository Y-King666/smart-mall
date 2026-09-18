package com.yking.malladmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yking.malladmin.dto.ProductForm;
import com.yking.malladmin.service.ProductService;
import com.yking.mallcommon.Result;
import com.yking.mallcommon.entity.PmsProduct;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端商品管理接口
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品列表（分页）
     *
     * keyword、categoryId、status 均可选，为空时不过滤。
     * categoryId、status 允许传空串，Spring 会将其转换为 null。
     */
    @GetMapping
    public Result<IPage<PmsProduct>> list(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long categoryId,
                                         @RequestParam(required = false) Integer status) {
        return Result.success(productService.page(page, size, keyword, categoryId, status));
    }

    /** 商品详情 */
    @GetMapping("/{id}")
    public Result<PmsProduct> detail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    /** 新增商品 */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ProductForm form) {
        productService.create(form);
        return Result.success();
    }

    /** 编辑商品 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductForm form) {
        productService.update(id, form);
        return Result.success();
    }

    /**
     * 切换上下架状态
     * 前端不传目标状态，由后端读取当前值取反
     */
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        productService.toggleStatus(id);
        return Result.success();
    }

    /** 删除商品（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
}
