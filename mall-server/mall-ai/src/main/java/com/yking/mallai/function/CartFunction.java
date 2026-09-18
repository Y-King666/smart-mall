package com.yking.mallai.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yking.mallcommon.entity.OmsCartItem;
import com.yking.mallcommon.entity.PmsProduct;
import com.yking.mallcommon.mapper.OmsCartItemMapper;
import com.yking.mallcommon.mapper.PmsProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户购物车查询工具
 *
 * 与 OrderFunction 同理：身份由构造参数带入，按请求新建实例，不做成单例 Bean。
 */
@Slf4j
public class CartFunction {

    /** 购物车项已选中 */
    private static final int CHECKED = 1;

    private final Long userId;
    private final OmsCartItemMapper omsCartItemMapper;
    private final PmsProductMapper pmsProductMapper;

    public CartFunction(Long userId, OmsCartItemMapper omsCartItemMapper, PmsProductMapper pmsProductMapper) {
        this.userId = userId;
        this.omsCartItemMapper = omsCartItemMapper;
        this.pmsProductMapper = pmsProductMapper;
    }

    @Tool(description = "查询当前登录用户购物车中的商品，返回每件商品的名称、单价、数量、是否已选中，以及已选中商品的总金额")
    public CartInfo getMyCart() {
        log.info("【ToolCall】getMyCart 查询用户 {} 的购物车", userId);

        List<OmsCartItem> cartItems = omsCartItemMapper.selectList(new LambdaQueryWrapper<OmsCartItem>()
                .eq(OmsCartItem::getUserId, userId)
                .orderByDesc(OmsCartItem::getId));
        if (cartItems.isEmpty()) {
            return new CartInfo(0, 0.0, List.of());
        }

        // 一次性取出涉及的商品，避免逐项查询数据库
        List<Long> productIds = cartItems.stream()
                .map(OmsCartItem::getProductId)
                .distinct()
                .toList();
        Map<Long, PmsProduct> productMap = pmsProductMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(PmsProduct::getId, product -> product));

        List<CartGoods> goodsList = new ArrayList<>(cartItems.size());
        BigDecimal checkedAmount = BigDecimal.ZERO;
        for (OmsCartItem cartItem : cartItems) {
            PmsProduct product = productMap.get(cartItem.getProductId());
            // 商品已被删除（逻辑删除后查不出来）时跳过该购物车项，与购物车页面的处理一致
            if (product == null) {
                continue;
            }

            boolean checked = cartItem.getChecked() != null && cartItem.getChecked() == CHECKED;
            goodsList.add(new CartGoods(product.getName(), product.getPrice().doubleValue(),
                    cartItem.getQuantity(), checked));
            if (checked) {
                checkedAmount = checkedAmount.add(
                        product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            }
        }

        CartInfo cart = new CartInfo(goodsList.size(), checkedAmount.doubleValue(), goodsList);
        log.info("【ToolCall】getMyCart 查询结果：{}", cart);
        return cart;
    }

    /**
     * 购物车查询结果(返回给AI的数据结构)
     *
     * checkedAmount 只是已选中商品的总金额，结算时也只结算选中的部分
     */
    public record CartInfo(int itemCount, double checkedAmount, List<CartGoods> goods) { }

    /**
     * 购物车中的一件商品(返回给AI的数据结构)
     */
    public record CartGoods(String productName, double price, int quantity, boolean checked) { }
}
