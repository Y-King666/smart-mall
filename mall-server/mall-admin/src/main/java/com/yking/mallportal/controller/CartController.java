package com.yking.mallportal.controller;

import com.yking.mallportal.dto.CartAddRequest;
import com.yking.mallportal.dto.CartItemVO;
import com.yking.mallportal.dto.CartUpdateRequest;
import com.yking.mallportal.service.CartService;
import com.yking.mallcommon.Result;
import com.yking.mallcommon.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
 * 用户端购物车接口
 *
 * 需要登录（既不是公开路径，也不属于需要 ADMIN 角色的 /api/admin/**），
 * 所有操作都限定在当前登录用户范围内。
 */
@RestController
@RequestMapping("/api/portal/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** 购物车列表 */
    @GetMapping
    public Result<List<CartItemVO>> list(Authentication authentication) {
        return Result.success(cartService.list(AuthUtil.currentUserId(authentication)));
    }

    /** 加入购物车，同一商品已存在时累加数量 */
    @PostMapping("/add")
    public Result<Void> add(Authentication authentication, @Valid @RequestBody CartAddRequest request) {
        cartService.add(AuthUtil.currentUserId(authentication), request);
        return Result.success();
    }

    /** 修改购物车项：数量、选中状态 */
    @PutMapping("/update")
    public Result<Void> update(Authentication authentication, @Valid @RequestBody CartUpdateRequest request) {
        cartService.update(AuthUtil.currentUserId(authentication), request);
        return Result.success();
    }

    /** 删除购物车项 */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(Authentication authentication, @PathVariable Long id) {
        cartService.delete(AuthUtil.currentUserId(authentication), id);
        return Result.success();
    }
}
