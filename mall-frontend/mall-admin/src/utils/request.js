/**
 * Axios 请求封装（管理后台）
 *
 * 统一处理：
 *   - 请求基础配置（baseURL、超时）
 *   - 请求拦截器：自动携带 JWT Token
 *   - 响应拦截器：解包 Result<T> 结构、统一错误提示、401 自动跳转登录
 *
 * 后端统一响应格式：
 *   { code: 200, message: "success", data: {...} }
 *
 * 经过响应拦截器后，API 调用直接返回 data 字段（内层数据），
 * 业务代码无需再 res.data 解包
 *
 * 使用方式：
 *   import request from '@/utils/request'
 *   const data = await request.get('/admin/products', { params: { page: 1 } })
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 创建 Axios 实例
 *
 * baseURL: 从环境变量 VITE_API_BASE_URL 读取
 *          开发环境通过 Vite 代理转发到后端 localhost:8080
 * timeout: 请求超时时间 15 秒（后台操作可能较慢）
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

/**
 * 请求拦截器
 *
 * 每次发送请求前自动从 localStorage 读取 token，
 * 添加到请求头 Authorization: Bearer <token>
 * 后端 JWT 过滤器据此验证管理员身份
 */
request.interceptors.request.use(
  (config) => { //每个请求发出之前自动执行，负责给请求头添加 JWT 令牌。
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {//失败回调
    return Promise.reject(error)//错误向下传递，让调用方去处理。
  }
)

/**
 * 响应拦截器 — 成功响应（HTTP 2xx）
 *
 * 处理逻辑：
 *   1. 响应含 code 字段：
 *      - code === 200 或 0：业务成功，解包返回 data 字段
 *      - code === 401 或 403：未授权/无权限，清除 token 并跳转登录页
 *      - 其他 code：业务错误，弹出错误提示并 reject
 *   2. 响应无 code 字段：直接返回原始数据（兼容非标准接口）
 */
request.interceptors.response.use(
  (response) => {//成功响应，负责解包后端的统一响应格式，让业务代码不用手动解包。
    const res = response.data

    // 标准 Result<T> 响应格式
    if (res.code !== undefined) {
      if (res.code === 200 || res.code === 0) {
        // 业务成功：解包返回内层 data（如果有的话）
        return res.data !== undefined ? res.data : res
      } else if (res.code === 401 || res.code === 403) {
        // 未授权或无权限：清除本地登录信息，跳转登录页
        const message = res.code === 401 ? '登录已过期，请重新登录' : '没有权限访问，请重新登录'
        ElMessage.error(res.message || message)
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
        return Promise.reject(new Error(res.message || message))
      } else {
        // 业务错误：弹出提示并 reject
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    }

    // 非标准响应（无 code 字段）：直接返回原始数据
    return res
  },

  /**
   * 响应拦截器 — 失败响应（HTTP 4xx/5xx、网络错误）
   *
   * 根据 HTTP 状态码给出对应中文提示：
   *   401: 登录过期 → 清除 token 并跳转登录页
   *   403: 无权限 → 清除 token 并跳转登录页
   *   404: 资源不存在
   *   500: 服务器错误
   *   无响应: 网络错误
   *   其他: 通用错误提示
   */
  (error) => {//失败响应，HTTP 4xx/5xx 或网络错误
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        // 未授权：清除本地登录信息，跳转登录页
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (status === 403) {
        // 无权限访问：清除本地登录信息，跳转登录页
        ElMessage.error('没有权限访问，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      } else {
        ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.request) {
      // 请求已发送但无响应（网络断开等）
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      // 请求配置错误
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

export default request
