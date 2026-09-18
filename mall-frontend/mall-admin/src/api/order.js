import request from '@/utils/request'

export function getOrders(params) {
  return request({ url: '/api/admin/orders', method: 'get', params })
}

export function getOrderDetail(id) {
  return request({ url: `/api/admin/orders/${id}`, method: 'get' })
}

export function getOrderStats() {
  return request({ url: '/api/admin/orders/stats', method: 'get' })
}
