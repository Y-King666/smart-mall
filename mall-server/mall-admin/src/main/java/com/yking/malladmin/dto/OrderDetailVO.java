package com.yking.malladmin.dto;

import com.yking.mallcommon.entity.OmsOrder;
import com.yking.mallcommon.entity.OmsOrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单详情数据
 *
 * 在订单列表字段基础上追加商品明细列表。管理端与用户端共用本结构。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends OrderVO {

    /** 订单商品明细 */
    private List<OmsOrderItem> items;

    /** 由订单实体与商品明细构建详情数据 */
    public static OrderDetailVO from(OmsOrder order, List<OmsOrderItem> items) {
        OrderDetailVO vo = copy(order, new OrderDetailVO());
        vo.setItems(items);
        return vo;
    }
}
