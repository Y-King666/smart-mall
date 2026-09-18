package com.yking.mallcommon.service;

import com.yking.mallcommon.entity.OmsOrder;

/**
 * 订单取消
 *
 * 放在公共模块，是因为有两个调用方：用户端的订单接口（mall-admin）与 AI 客服的工具（mall-ai）。
 * 两者必须走同一套规则——恢复库存、扣回销量、校验归属——否则数据会对不上。
 */
public interface OrderCancelService {

    /**
     * 按订单号查当前用户自己的订单
     *
     * @return 查不到（不存在或不属于该用户）时返回 null，由调用方决定怎么提示；
     *         两种情况返回同一结果，避免通过提示差异探测他人订单是否存在
     */
    OmsOrder findByOrderNo(Long userId, String orderNo);

    /**
     * 取消订单
     *
     * 待支付与已支付订单都可取消：待支付只恢复库存；
     * 已支付在支付时累加过销量，取消时要把销量扣回。
     *
     * @throws com.yking.mallcommon.exception.BusinessException
     *         订单不存在 / 不属于该用户 / 已经取消过
     */
    void cancel(Long userId, Long orderId);
}
