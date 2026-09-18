package com.yking.malladmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yking.malladmin.dto.OrderDetailVO;
import com.yking.malladmin.dto.OrderStatsVO;
import com.yking.malladmin.dto.OrderVO;
import com.yking.malladmin.service.OrderService;
import com.yking.mallcommon.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端订单管理接口
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 订单列表（分页）
     *
     * orderNo、payStatus 均可选，为空时不过滤。
     */
    @GetMapping
    public Result<IPage<OrderVO>> list(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String orderNo,
                                       @RequestParam(required = false) Integer payStatus) {
        return Result.success(orderService.page(page, size, orderNo, payStatus));
    }

    /**
     * 订单统计汇总
     *
     * 映射路径为字面量 /stats，优先级高于 /{id}，不会被当作订单 ID 匹配
     */
    @GetMapping("/stats")
    public Result<OrderStatsVO> stats() {
        return Result.success(orderService.stats());
    }

    /** 订单详情（含商品明细） */
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detail(id));
    }
}
