package com.yking.malladmin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yking.malladmin.dto.OrderDetailVO;
import com.yking.malladmin.dto.OrderStatsVO;
import com.yking.malladmin.dto.OrderVO;

/**
 * 订单管理业务逻辑
 */
public interface OrderService {

    /**
     * 分页查询订单
     *
     * @param orderNo   订单号模糊关键字，为空则不过滤
     * @param payStatus 支付状态，为 null 则不过滤
     */
    IPage<OrderVO> page(long page, long size, String orderNo, Integer payStatus);

    /** 查询订单详情（含商品明细） */
    OrderDetailVO detail(Long id);

    /** 订单统计汇总 */
    OrderStatsVO stats();
}
