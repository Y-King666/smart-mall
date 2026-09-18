/**
 * 商品浏览接口（公开，无需登录）
 */
import request from '@/utils/request'

/**
 * 商品列表（分页）
 * @param {Object} params { page, size, keyword, categoryId }，均可选
 */
export function getProducts(params) {
  return request({ url: '/api/portal/products', method: 'get', params })
}

/** 商品详情 */
export function getProduct(id) {
  return request({ url: `/api/portal/products/${id}`, method: 'get' })
}

/** 分类树 */
export function getCategories() {
  return request({ url: '/api/portal/categories', method: 'get' })
}
