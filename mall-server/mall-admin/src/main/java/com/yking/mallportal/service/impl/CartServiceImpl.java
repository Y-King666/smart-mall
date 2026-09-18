package com.yking.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yking.mallportal.dto.CartAddRequest;
import com.yking.mallportal.dto.CartItemVO;
import com.yking.mallportal.dto.CartUpdateRequest;
import com.yking.mallportal.service.CartService;
import com.yking.mallcommon.entity.OmsCartItem;
import com.yking.mallcommon.entity.PmsProduct;
import com.yking.mallcommon.exception.BusinessException;
import com.yking.mallcommon.mapper.OmsCartItemMapper;
import com.yking.mallcommon.mapper.PmsProductMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    /** 商品上架状态 */
    private static final int STATUS_ON_SALE = 1;

    /** 购物车项已选中 */
    private static final int CHECKED = 1;

    private final OmsCartItemMapper omsCartItemMapper;
    private final PmsProductMapper pmsProductMapper;

    public CartServiceImpl(OmsCartItemMapper omsCartItemMapper, PmsProductMapper pmsProductMapper) {
        this.omsCartItemMapper = omsCartItemMapper;
        this.pmsProductMapper = pmsProductMapper;
    }

    @Override
    public List<CartItemVO> list(Long userId) {
        List<OmsCartItem> cartItems = omsCartItemMapper.selectList(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getUserId, userId)
                        .orderByDesc(OmsCartItem::getId));
        if (cartItems.isEmpty()) {
            return new ArrayList<>();
        }

        // 一次性取出涉及的商品，避免逐项查询数据库
        List<Long> productIds = cartItems.stream()
                .map(OmsCartItem::getProductId)
                .distinct()
                .toList();
        Map<Long, PmsProduct> productMap = pmsProductMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(PmsProduct::getId, product -> product));

        List<CartItemVO> result = new ArrayList<>(cartItems.size());
        for (OmsCartItem cartItem : cartItems) {
            PmsProduct product = productMap.get(cartItem.getProductId());
            // 商品已被删除（逻辑删除后查不出来）时跳过该购物车项
            if (product == null) {
                continue;
            }
            CartItemVO vo = new CartItemVO();
            vo.setId(cartItem.getId());
            vo.setProductId(product.getId());
            vo.setName(product.getName());
            vo.setCoverImage(product.getCoverImage());
            vo.setPrice(product.getPrice());
            vo.setStock(product.getStock());
            vo.setStatus(product.getStatus());
            vo.setQuantity(cartItem.getQuantity());
            vo.setChecked(cartItem.getChecked());
            result.add(vo);
        }
        return result;
    }

    @Override
    public void add(Long userId, CartAddRequest request) {
        PmsProduct product = pmsProductMapper.selectById(request.getProductId());
        if (product == null || product.getStatus() != STATUS_ON_SALE) {
            throw new BusinessException("商品不存在或已下架");
        }

        // (user_id, product_id) 没有唯一约束，用 selectList 取首条，避免历史重复数据让查询直接报错
        List<OmsCartItem> existingItems = omsCartItemMapper.selectList(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getUserId, userId)
                        .eq(OmsCartItem::getProductId, request.getProductId())
                        .orderByAsc(OmsCartItem::getId));
        OmsCartItem existing = existingItems.isEmpty() ? null : existingItems.get(0);

        // 同一商品已在购物车中时累加数量
        int quantity = request.getQuantity() + (existing == null ? 0 : existing.getQuantity());
        // 加购时就校验库存，避免下单结算时才发现买不了
        if (quantity > product.getStock()) {
            throw new BusinessException("库存不足，当前库存 " + product.getStock());
        }

        if (existing == null) {
            OmsCartItem cartItem = new OmsCartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(quantity);
            // 选中状态由数据库默认值填充为 1（已选中）
            omsCartItemMapper.insert(cartItem);
        } else {
            omsCartItemMapper.update(null, new LambdaUpdateWrapper<OmsCartItem>()
                    .eq(OmsCartItem::getId, existing.getId())
                    .set(OmsCartItem::getQuantity, quantity)
                    // 刚加购的商品默认勾选，方便直接结算
                    .set(OmsCartItem::getChecked, CHECKED));
        }
    }

    @Override
    public void update(Long userId, CartUpdateRequest request) {
        if (request.getQuantity() == null && request.getChecked() == null) {
            throw new BusinessException("请提供要修改的数量或选中状态");
        }

        // 同时限定 userId，避免猜 ID 修改到他人的购物车
        OmsCartItem cartItem = omsCartItemMapper.selectOne(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getId, request.getId())
                        .eq(OmsCartItem::getUserId, userId));
        if (cartItem == null) {
            throw new BusinessException("购物车项不存在");
        }

        LambdaUpdateWrapper<OmsCartItem> wrapper = new LambdaUpdateWrapper<OmsCartItem>()
                .eq(OmsCartItem::getId, cartItem.getId());

        if (request.getQuantity() != null) {
            // 改数量时重新校验库存
            PmsProduct product = pmsProductMapper.selectById(cartItem.getProductId());
            if (product == null || product.getStatus() != STATUS_ON_SALE) {
                throw new BusinessException("商品不存在或已下架");
            }
            if (request.getQuantity() > product.getStock()) {
                throw new BusinessException("库存不足，当前库存 " + product.getStock());
            }
            wrapper.set(OmsCartItem::getQuantity, request.getQuantity());
        }
        if (request.getChecked() != null) {
            wrapper.set(OmsCartItem::getChecked, request.getChecked());
        }
        omsCartItemMapper.update(null, wrapper);
    }

    @Override
    public void delete(Long userId, Long id) {
        // 同时限定 userId，避免猜 ID 删除到他人的购物车
        int rows = omsCartItemMapper.delete(new LambdaQueryWrapper<OmsCartItem>()
                .eq(OmsCartItem::getId, id)
                .eq(OmsCartItem::getUserId, userId));
        if (rows == 0) {
            throw new BusinessException("购物车项不存在");
        }
    }
}
