/**
 * Axios 请求封装（用户端商城）
 *
 * 约定与后端、管理端保持一致：
 *   后端统一响应 { code, message, data }
 *   本封装在响应拦截器里解包，业务代码拿到的是内层 data，无需再 .data
 *
 * 与管理端的差异：未登录时跳登录页并带上 redirect 参数，
 * 登录成功后能回到用户原本想访问的页面。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/** 本地存储中令牌与用户信息的键名 */
export const TOKEN_KEY = 'portal_token'
export const USER_KEY = 'portal_user'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

/** 清除本地登录态 */
function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

/** 携带当前地址跳转登录页，登录后可跳回 */
function redirectToLogin(message) {
  ElMessage.error(message)
  clearAuth()
  const current = router.currentRoute.value
  if (current.path !== '/login') {
    router.push({ path: '/login', query: { redirect: current.fullPath } })
  }
}

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code === undefined) {
      // 非标准响应，直接返回原数据
      return res
    }

    if (res.code === 200 || res.code === 0) {
      return res.data !== undefined ? res.data : res
    }

    if (res.code === 401 || res.code === 403) {
      redirectToLogin(res.message || '请先登录')
      return Promise.reject(new Error(res.message || '请先登录'))
    }

    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401 || status === 403) {
        redirectToLogin('请先登录')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      } else {
        ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

export default request
