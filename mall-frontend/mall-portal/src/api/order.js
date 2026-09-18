/**
 * 订单接口（需登录，只能操作自己的订单）
 */
import request from '@/utils/request'

/**
 * 提交订单：把购物车中已选中的商品下单
 * 后端不接受商品参数，商品来自购物车 checked=1 的项
 */
export function submitOrder() {
  return request({ url: '/api/portal/orders', method: 'post' })
}

/** 我的订单列表（分页，每笔含商品明细） */
export function getOrders(params) {
  return request({ url: '/api/portal/orders', method: 'get', params })
}

/**
 * 立即购买：直接下单并支付，不经过购物车，返回订单号
 * 用于商品详情页的「立即购买」弹层
 */
export function submitDirectOrder(data) {
  return request({ url: '/api/portal/orders/direct', method: 'post', data })
}

/** 支付订单（模拟支付） */
export function payOrder(id) {
  return request({ url: `/api/portal/orders/${id}/pay`, method: 'post' })
}

/** 取消订单 */
export function cancelOrder(id) {
  return request({ url: `/api/portal/orders/${id}/cancel`, method: 'post' })
}
