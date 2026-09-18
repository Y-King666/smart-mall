package com.yking.malladmin.controller;

import com.yking.malladmin.dto.CategoryForm;
import com.yking.malladmin.service.CategoryService;
import com.yking.mallcommon.Result;
import com.yking.mallcommon.entity.PmsCategory;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端分类管理接口
 */
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 分类树
     * 返回顶级分类列表，子分类通过 children 字段逐级嵌套
     */
    @GetMapping
    public Result<List<PmsCategory>> tree() {
        return Result.success(categoryService.getTree());
    }

    /** 新增分类 */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategoryForm form) {
        categoryService.create(form);
        return Result.success();
    }

    /** 编辑分类（id 通过路径传递，请求体不含 id） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryForm form) {
        categoryService.update(id, form);
        return Result.success();
    }

    /** 删除分类（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
