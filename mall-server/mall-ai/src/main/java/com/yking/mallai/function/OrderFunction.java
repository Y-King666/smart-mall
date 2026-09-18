package com.yking.mallai.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yking.mallcommon.entity.OmsOrder;
import com.yking.mallcommon.entity.OmsOrderItem;
import com.yking.mallcommon.exception.BusinessException;
import com.yking.mallcommon.mapper.OmsOrderItemMapper;
import com.yking.mallcommon.mapper.OmsOrderMapper;
import com.yking.mallcommon.service.OrderCancelService;
import com.yking.mallcommon.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * 用户订单工具
 *
 * 只能查、也只能操作当前登录用户自己的订单：身份由构造参数带入，而不是在工具内部去读 SecurityContext——
 * 工具是模型回调时才执行的，那时未必还跑在原请求线程上，ThreadLocal 里的登录信息不可靠。
 * 因此本类不做成单例 Bean，由 AiChatServiceImpl 在每次对话时按用户新建。
 */
@Slf4j
public class OrderFunction {

    /** 最多返回的订单笔数：太多了会挤占模型上下文，也会把回答带偏 */
    private static final int MAX_ORDERS = 5;

    /** 支付状态：待支付 */
    private static final int STATUS_PENDING = 0;

    /** 支付状态：已支付 */
    private static final int STATUS_PAID = 1;

    /** 支付状态：已取消 */
    private static final int STATUS_CANCELLED = 2;

    private final Long userId;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final OrderCancelService orderCancelService;

    public OrderFunction(Long userId,
                         OmsOrderMapper omsOrderMapper,
                         OmsOrderItemMapper omsOrderItemMapper,
                         OrderCancelService orderCancelService) {
        this.userId = userId;
        this.omsOrderMapper = omsOrderMapper;
        this.omsOrderItemMapper = omsOrderItemMapper;
        this.orderCancelService = orderCancelService;
    }

    /**
     * 取消订单
     *
     * 会改动数据（订单状态、库存、销量），因此描述里写清调用前提，让模型只在用户明确要求
     * 且已给出订单号时才调用。
     *
     * 失败不抛异常而是返回一句说明：模型拿到文字后可以自然地转述给用户，
     * 抛出去只会变成一条「AI 服务不可用」的兜底文案。
     */
    @Tool(description = "按订单号取消当前用户自己的一笔订单，取消后商品库存会恢复。"
            + "只在用户明确要求取消订单、并且已经提供了订单号时调用")
    public String cancelMyOrder(@ToolParam(description = "用户要取消的订单号，是用户提供的那串数字") String orderNo) {
        log.info("【ToolCall】cancelMyOrder 用户 {} 请求取消订单 {}", userId, orderNo);

        OmsOrder order = orderCancelService.findByOrderNo(userId, orderNo);
        if (order == null) {
            return "没有查到你名下订单号为「" + orderNo + "」的订单，请确认订单号是否正确。";
        }

        try {
            orderCancelService.cancel(userId, order.getId());
        } catch (BusinessException e) {
            return "订单「" + orderNo + "」取消失败：" + e.getMessage() + "。";
        }
        // 只回报结果本身：库存变化是商城内部的处理，用户不需要知道
        return "订单「" + orderNo + "」已取消。";
    }

    @Tool(description = "查询当前登录用户自己的历史订单，返回订单总数与最近几笔订单的订单号、下单时间、金额、支付状态和商品明细")
    public OrderHistory getMyOrders() {
        log.info("【ToolCall】getMyOrders 查询用户 {} 的历史订单", userId);

        // 单独统计总笔数：只给最近几笔的话，模型回答「一共下过几单」时只能自己编一个数
        Long total = omsOrderMapper.selectCount(
                new LambdaQueryWrapper<OmsOrder>().eq(OmsOrder::getUserId, userId));

        List<OmsOrder> orders = omsOrderMapper.selectList(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getUserId, userId)
                .orderByDesc(OmsOrder::getId)
                // 常量拼接，无外部输入
                .last("limit " + MAX_ORDERS));

        List<OrderInfo> orderInfos = orders.stream().map(order -> new OrderInfo(
                order.getOrderNo(),
                DateTimeUtil.format(order.getCreateTime()),
                order.getTotalAmount().doubleValue(),
                payStatusText(order.getPayStatus()),
                listItems(order.getId())
        )).toList();

        OrderHistory history = new OrderHistory(total == null ? 0L : total, orderInfos);
        log.info("【ToolCall】getMyOrders 查询结果：{}", history);
        return history;
    }

    /** 订单明细按下单时的名称快照与数量拼成「商品名 x 件数」 */
    private List<String> listItems(Long orderId) {
        return omsOrderItemMapper.selectList(
                        new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId))
                .stream()
                .map(item -> item.getProductName() + " x" + item.getQuantity())
                .toList();
    }

    /** 支付状态转成模型与用户都读得懂的中文 */
    private String payStatusText(Integer payStatus) {
        if (payStatus == null) {
            return "未知";
        }
        return switch (payStatus) {
            case STATUS_PENDING -> "待支付";
            case STATUS_PAID -> "已支付";
            case STATUS_CANCELLED -> "已取消";
            default -> "未知";
        };
    }

    /**
     * 订单查询结果(返回给AI的数据结构)
     *
     * totalCount 是一共下过多少单，recentOrders 只是其中最近几笔
     */
    public record OrderHistory(long totalCount, List<OrderInfo> recentOrders) { }

    /**
     * 单笔订单(返回给AI的数据结构)
     */
    public record OrderInfo(String orderNo, String createTime, double totalAmount,
                            String payStatus, List<String> items) { }
}
