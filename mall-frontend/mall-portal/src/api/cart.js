/**
 * 购物车接口（需登录）
 */
import request from '@/utils/request'

/** 购物车列表 */
export function getCart() {
  return request({ url: '/api/portal/cart', method: 'get' })
}

/** 加入购物车，同一商品已存在时后端会累加数量 */
export function addToCart(data) {
  return request({ url: '/api/portal/cart/add', method: 'post', data })
}

/**
 * 修改购物车项
 * @param {Object} data { id, quantity?, checked? }，未传的字段保持不变
 */
export function updateCartItem(data) {
  return request({ url: '/api/portal/cart/update', method: 'put', data })
}

/** 删除购物车项 */
export function deleteCartItem(id) {
  return request({ url: `/api/portal/cart/delete/${id}`, method: 'delete' })
}
