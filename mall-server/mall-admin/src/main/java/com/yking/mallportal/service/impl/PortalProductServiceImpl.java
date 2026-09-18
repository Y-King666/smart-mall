package com.yking.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yking.mallportal.service.PortalProductService;
import com.yking.mallcommon.entity.PmsProduct;
import com.yking.mallcommon.exception.BusinessException;
import com.yking.mallcommon.mapper.PmsProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PortalProductServiceImpl implements PortalProductService {

    /** 商品上架状态 */
    private static final int STATUS_ON_SALE = 1;

    /** 排序方式：按销量倒序（热销榜） */
    private static final String SORT_SALES = "sales";

    private final PmsProductMapper pmsProductMapper;

    public PortalProductServiceImpl(PmsProductMapper pmsProductMapper) {
        this.pmsProductMapper = pmsProductMapper;
    }

    @Override
    public IPage<PmsProduct> page(long page, long size, String keyword, Long categoryId, String sort) {
        // 与后台管理的区别：用户端只能看到上架商品
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<PmsProduct>()
                .eq(PmsProduct::getStatus, STATUS_ON_SALE)
                .like(StringUtils.hasText(keyword), PmsProduct::getName, keyword)
                .eq(categoryId != null, PmsProduct::getCategoryId, categoryId);

        if (SORT_SALES.equals(sort)) {
            // 热销榜必须由数据库整体排序后取前几条：销量相同的再按 id 兜底，保证顺序稳定
            wrapper.orderByDesc(PmsProduct::getSalesCount).orderByDesc(PmsProduct::getId);
        } else {
            wrapper.orderByDesc(PmsProduct::getId);
        }
        return pmsProductMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public PmsProduct getById(Long id) {
        PmsProduct product = pmsProductMapper.selectById(id);
        // 下架商品对用户端不可见，统一按「不存在」提示，不暴露商品是否曾经存在
        if (product == null || product.getStatus() != STATUS_ON_SALE) {
            throw new BusinessException("商品不存在或已下架");
        }
        return product;
    }
}
