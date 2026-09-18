package com.yking.malladmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yking.malladmin.dto.CategoryForm;
import com.yking.malladmin.service.CategoryService;
import com.yking.mallcommon.entity.PmsCategory;
import com.yking.mallcommon.exception.BusinessException;
import com.yking.mallcommon.mapper.PmsCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    /** 顶级分类的 parentId 约定值，前端新增顶级分类时提交的也是 0 */
    private static final Long TOP_PARENT_ID = 0L;

    private final PmsCategoryMapper pmsCategoryMapper;

    public CategoryServiceImpl(PmsCategoryMapper pmsCategoryMapper) {
        this.pmsCategoryMapper = pmsCategoryMapper;
    }

    @Override
    public List<PmsCategory> getTree() {
        List<PmsCategory> allCategories = pmsCategoryMapper.selectList(
                new LambdaQueryWrapper<PmsCategory>().orderByAsc(PmsCategory::getSort));

        // 先按父分类分组，再一次性挂到各节点上，避免逐层查询数据库
        Map<Long, List<PmsCategory>> childrenByParent = allCategories.stream()
                .collect(Collectors.groupingBy(PmsCategory::getParentId));
        allCategories.forEach(category -> category.setChildren(
                childrenByParent.getOrDefault(category.getId(), new ArrayList<>())));

        return childrenByParent.getOrDefault(TOP_PARENT_ID, new ArrayList<>());
    }

    @Override
    public void create(CategoryForm form) {
        PmsCategory category = new PmsCategory();
        category.setName(form.getName());
        category.setParentId(form.getParentId());
        category.setSort(form.getSort());
        // parentId、sort 为 null 时由数据库默认值填充
        pmsCategoryMapper.insert(category);
    }

    @Override
    public void update(Long id, CategoryForm form) {
        PmsCategory current = pmsCategoryMapper.selectById(id);
        if (current == null) {
            throw new BusinessException("分类不存在");
        }
        // 父分类指向自身会让分类树出现自引用，序列化时造成无限递归
        if (id.equals(form.getParentId())) {
            throw new BusinessException("父分类不能选择自己");
        }

        // parentId、sort 在数据库中为 NOT NULL，入参为空时回退到默认值
        LambdaUpdateWrapper<PmsCategory> wrapper = new LambdaUpdateWrapper<PmsCategory>()
                .eq(PmsCategory::getId, id)
                .set(PmsCategory::getName, form.getName())
                .set(PmsCategory::getParentId, form.getParentId() == null ? TOP_PARENT_ID : form.getParentId())
                .set(PmsCategory::getSort, form.getSort() == null ? 0 : form.getSort());
        pmsCategoryMapper.update(null, wrapper);
    }

    @Override
    public void delete(Long id) {
        // 存在子分类时禁止删除，避免子分类变成永远不会出现在树上的游离节点
        Long childCount = pmsCategoryMapper.selectCount(
                new LambdaQueryWrapper<PmsCategory>().eq(PmsCategory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该分类下还有子分类，请先删除子分类");
        }
        if (pmsCategoryMapper.deleteById(id) == 0) {
            throw new BusinessException("分类不存在");
        }
    }
}
