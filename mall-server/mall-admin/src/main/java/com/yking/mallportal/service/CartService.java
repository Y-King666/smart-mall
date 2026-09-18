package com.yking.mallportal.service;

import com.yking.mallportal.dto.CartAddRequest;
import com.yking.mallportal.dto.CartUpdateRequest;
import com.yking.mallportal.dto.CartItemVO;

import java.util.List;

/**
 * 购物车业务逻辑
 *
 * 所有方法都以当前登录用户为作用域，不会操作到他人的购物车。
 */
public interface CartService {

    /** 查询当前用户的购物车列表 */
    List<CartItemVO> list(Long userId);

    /** 加入购物车，同一商品已存在时累加数量 */
    void add(Long userId, CartAddRequest request);

    /** 修改购物车项的数量或选中状态 */
    void update(Long userId, CartUpdateRequest request);

    /** 删除购物车项 */
    void delete(Long userId, Long id);
}
