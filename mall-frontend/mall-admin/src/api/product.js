import request from '@/utils/request'

export function getProducts(params) {
  return request({ url: '/api/admin/products', method: 'get', params })
}

export function getProduct(id) {
  return request({ url: `/api/admin/products/${id}`, method: 'get' })
}

export function createProduct(data) {
  return request({ url: '/api/admin/products', method: 'post', data })
}

export function updateProduct(id, data) {
  return request({ url: `/api/admin/products/${id}`, method: 'put', data })
}

export function toggleProductStatus(id) {
  return request({ url: `/api/admin/products/${id}/status`, method: 'put' })
}

export function deleteProduct(id) {
  return request({ url: `/api/admin/products/${id}`, method: 'delete' })
}
