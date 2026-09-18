package com.yking.mallportal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yking.malladmin.dto.OrderDetailVO;
import com.yking.mallportal.dto.DirectOrderRequest;
import com.yking.mallportal.service.PortalOrderService;
import com.yking.mallcommon.Result;
import com.yking.mallcommon.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端订单接口
 *
 * 需要登录，查询与操作都会校验订单归属，只能看到自己的订单。
 */
@RestController
@RequestMapping("/api/portal/orders")
public class OrderController {

    private final PortalOrderService portalOrderService;

    public OrderController(PortalOrderService portalOrderService) {
        this.portalOrderService = portalOrderService;
    }

    /** 提交订单：把购物车中已选中的商品下单 */
    @PostMapping
    public Result<Void> submit(Authentication authentication) {
        portalOrderService.submit(AuthUtil.currentUserId(authentication));
        return Result.success();
    }

    /** 直接下单并支付（立即购买，不经过购物车） */
    @PostMapping("/direct")
    public Result<String> submitDirect(Authentication authentication,
                                       @Valid @RequestBody DirectOrderRequest request) {
        return Result.success(portalOrderService.submitDirect(AuthUtil.currentUserId(authentication), request));
    }

    /** 我的订单列表（分页，含每笔订单的商品明细） */
    @GetMapping
    public Result<IPage<OrderDetailVO>> list(Authentication authentication,
                                             @RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size) {
        return Result.success(portalOrderService.page(AuthUtil.currentUserId(authentication), page, size));
    }

    /** 支付订单（模拟支付） */
    @PostMapping("/{id}/pay")
    public Result<Void> pay(Authentication authentication, @PathVariable Long id) {
        portalOrderService.pay(AuthUtil.currentUserId(authentication), id);
        return Result.success();
    }

    /** 取消订单 */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(Authentication authentication, @PathVariable Long id) {
        portalOrderService.cancel(AuthUtil.currentUserId(authentication), id);
        return Result.success();
    }
}
