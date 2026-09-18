package com.yking.malladmin.service;

import com.yking.malladmin.dto.CategoryForm;
import com.yking.mallcommon.entity.PmsCategory;

import java.util.List;

/**
 * 分类管理业务逻辑
 */
public interface CategoryService {

    /**
     * 查询分类树
     *
     * @return 顶级分类列表，子分类挂在各级节点的 children 字段上
     */
    List<PmsCategory> getTree();

    /** 新增分类 */
    void create(CategoryForm form);

    /** 编辑分类 */
    void update(Long id, CategoryForm form);

    /** 删除分类（逻辑删除） */
    void delete(Long id);
}
