import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/api/auth/info',
    method: 'get'
  })
}

/**
 * 退出登录：通知后端吊销当前令牌
 * @param {string} token 令牌原文
 *
 * 这里显式携带令牌，而不是依赖请求拦截器从 localStorage 读取：
 * 调用方会先清空本地登录态，拦截器那时已经取不到令牌了
 */
export function logout(token) {
  return request({
    url: '/api/auth/logout',
    method: 'post',
    headers: { Authorization: `Bearer ${token}` }
  })
}
