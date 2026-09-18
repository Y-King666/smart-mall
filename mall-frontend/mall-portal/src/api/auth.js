/**
 * 认证相关接口
 */
import request from '@/utils/request'

/** 登录 */
export function login(data) {
  return request({ url: '/api/auth/login', method: 'post', data })
}

/** 注册 */
export function register(data) {
  return request({ url: '/api/auth/register', method: 'post', data })
}

/** 获取当前登录用户信息 */
export function getUserInfo() {
  return request({ url: '/api/auth/info', method: 'get' })
}

/**
 * 退出登录：通知后端吊销令牌
 *
 * 显式携带令牌，因为调用方会先清空本地登录态，拦截器那时已取不到令牌
 */
export function logout(token) {
  return request({
    url: '/api/auth/logout',
    method: 'post',
    headers: { Authorization: `Bearer ${token}` }
  })
}
