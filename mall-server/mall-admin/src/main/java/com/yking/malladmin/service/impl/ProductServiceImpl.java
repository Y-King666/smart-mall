package com.yking.malladmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yking.malladmin.dto.ProductForm;
import com.yking.malladmin.service.ProductService;
import com.yking.mallcommon.entity.PmsCategory;
import com.yking.mallcommon.entity.PmsProduct;
import com.yking.mallcommon.exception.BusinessException;
import com.yking.mallcommon.mapper.PmsCategoryMapper;
import com.yking.mallcommon.mapper.PmsProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    private final PmsProductMapper pmsProductMapper;
    private final PmsCategoryMapper pmsCategoryMapper;

    public ProductServiceImpl(PmsProductMapper pmsProductMapper, PmsCategoryMapper pmsCategoryMapper) {
        this.pmsProductMapper = pmsProductMapper;
        this.pmsCategoryMapper = pmsCategoryMapper;
    }

    @Override
    public IPage<PmsProduct> page(long page, long size, String keyword, Long categoryId, Integer status) {
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<PmsProduct>()
                .like(StringUtils.hasText(keyword), PmsProduct::getName, keyword)
                // 商品挂在叶子分类上，父分类只是分组：按分类筛选要连子分类一起查，
                // 否则选「数字课程」这种父分类永远查不到东西
                .in(categoryId != null, PmsProduct::getCategoryId,
                        categoryId == null ? List.of() : listSelfAndDescendantIds(categoryId))
                .eq(status != null, PmsProduct::getStatus, status)
                // 按 id 倒序，保证分页结果稳定
                .orderByDesc(PmsProduct::getId);
        return pmsProductMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 取某个分类及其全部后代的 id
     *
     * 分类表以 parent_id 自关联。按当前的两级结构，一次查全表再在内存里展开就够了；
     * 仍然写成逐层展开，日后加层级不用改这里。
     */
    private List<Long> listSelfAndDescendantIds(Long categoryId) {
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (PmsCategory category : pmsCategoryMapper.selectList(null)) {
            childrenByParent.computeIfAbsent(category.getParentId(), key -> new ArrayList<>())
                    .add(category.getId());
        }

        List<Long> ids = new ArrayList<>();
        Deque<Long> pending = new ArrayDeque<>();
        pending.push(categoryId);
        while (!pending.isEmpty()) {
            Long id = pending.pop();
            ids.add(id);
            for (Long child : childrenByParent.getOrDefault(id, List.of())) {
                pending.push(child);
            }
        }
        return ids;
    }

    @Override
    public PmsProduct getById(Long id) {
        PmsProduct product = pmsProductMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    @Override
    public void create(ProductForm form) {
        PmsProduct product = new PmsProduct();
        product.setName(form.getName());
        product.setCategoryId(form.getCategoryId());
        product.setPrice(form.getPrice());
        product.setOriginalPrice(form.getOriginalPrice());
        product.setStock(form.getStock());
        product.setCoverImage(form.getCoverImage());
        product.setDescription(form.getDescription());
        product.setDetail(form.getDetail());
        product.setStatus(form.getStatus());
        // 销量、逻辑删除标记、创建时间由数据库默认值填充
        pmsProductMapper.insert(product);
    }

    @Override
    public void update(Long id, ProductForm form) {
        getById(id);

        // 显式 set 全部表单字段：updateById 会跳过值为 null 的字段，
        // 导致编辑时把「原价」等内容清空后无法保存
        LambdaUpdateWrapper<PmsProduct> wrapper = new LambdaUpdateWrapper<PmsProduct>()
                .eq(PmsProduct::getId, id)
                .set(PmsProduct::getName, form.getName())
                .set(PmsProduct::getCategoryId, form.getCategoryId())
                .set(PmsProduct::getPrice, form.getPrice())
                .set(PmsProduct::getOriginalPrice, form.getOriginalPrice())
                .set(PmsProduct::getStock, form.getStock())
                .set(PmsProduct::getCoverImage, form.getCoverImage())
                .set(PmsProduct::getDescription, form.getDescription())
                .set(PmsProduct::getDetail, form.getDetail())
                .set(PmsProduct::getStatus, form.getStatus());
        pmsProductMapper.update(null, wrapper);
    }

    @Override
    public void toggleStatus(Long id) {
        // 直接在 SQL 中取反，避免「查询-修改-写回」之间的竞态
        int rows = pmsProductMapper.update(null, new LambdaUpdateWrapper<PmsProduct>()
                .eq(PmsProduct::getId, id)
                .setSql("status = 1 - status"));
        if (rows == 0) {
            throw new BusinessException("商品不存在");
        }
    }

    @Override
    public void delete(Long id) {
        // 实体标注了 @TableLogic，此处执行的是逻辑删除（更新 deleted = 1）
        if (pmsProductMapper.deleteById(id) == 0) {
            throw new BusinessException("商品不存在");
        }
    }
}
