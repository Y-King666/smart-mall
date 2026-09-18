package com.yking.mallportal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yking.malladmin.dto.OrderDetailVO;
import com.yking.mallportal.dto.DirectOrderRequest;

/**
 * 用户端订单业务逻辑
 *
 * 所有方法都以当前登录用户为作用域，查询与操作都会校验订单归属。
 */
public interface PortalOrderService {

    /** 提交订单：把购物车中已选中的商品下单 */
    void submit(Long userId);

    /** 直接下单并支付（立即购买，不走购物车），返回订单号 */
    String submitDirect(Long userId, DirectOrderRequest request);

    /** 支付订单（模拟支付） */
    void pay(Long userId, Long orderId);

    /** 分页查询我的订单，每笔都带上商品明细 */
    IPage<OrderDetailVO> page(Long userId, long page, long size);

    /** 取消订单 */
    void cancel(Long userId, Long orderId);
}
