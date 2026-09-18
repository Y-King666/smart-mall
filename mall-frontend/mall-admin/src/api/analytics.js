/**
 * 数据分析 API 接口
 */
import request from '@/utils/request'

/**
 * 获取销售统计数据
 * @param {number} days 统计天数（默认 7 天）
 */
export function getSalesStats(days = 7) {
  return request({
    url: '/api/admin/analytics/sales',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取用户价值分析
 */
export function getUserAnalysis() {
  return request({
    url: '/api/admin/analytics/users',
    method: 'get'
  })
}

/**
 * 获取商品推荐
 * @param {number} categoryId 分类 ID（可选）
 * @param {number} limit 返回数量（默认 20）
 */
export function getRecommendations(categoryId = null, limit = 20) {
  return request({
    url: '/api/admin/analytics/recommend',
    method: 'get',
    params: { categoryId, limit }
  })
}

/**
 * 获取用户个性化推荐
 * @param {number} userId 用户 ID
 * @param {number} limit 返回数量（默认 10）
 */
export function getUserRecommendations(userId, limit = 10) {
  return request({
    url: `/api/admin/analytics/recommend/user/${userId}`,
    method: 'get',
    params: { limit }
  })
}

/**
 * 健康检查
 */
export function checkAnalyticsHealth() {
  return request({
    url: '/api/admin/analytics/health',
    method: 'get'
  })
}
