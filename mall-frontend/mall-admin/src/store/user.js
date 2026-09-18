/**
 * 管理员用户状态管理（Pinia Store）
 *
 * 管理后台管理员的登录状态、用户信息、token
 * 数据持久化到 localStorage，页面刷新后保持登录态
 *
 * 使用方式：
 *   import { useUserStore } from '@/store/user'
 *   const userStore = useUserStore()
 *   await userStore.login({ username, password })
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
// 导入 API 函数并重命名，避免与 store 方法同名导致无限递归
import { login as loginApi, getUserInfo as getInfoApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // ===== 响应式状态 =====

  /** JWT 令牌，从 localStorage 恢复（刷新页面后保持登录） */
  const token = ref(localStorage.getItem('token') || '')

  /** 管理员用户信息对象（含 username、role、nickname 等） */
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // ===== 操作方法 =====

  /**
   * 管理员登录
   * @param {Object} form - { username: string, password: string }
   * @returns {Promise<Object>} 登录响应数据（含 token、用户信息）
   */
  async function login(form) {
    const res = await loginApi(form)
    // 防御性处理：兼容拦截器解包前后的两种情况
    const data = res?.data ?? res
    if (!data?.token) {
      throw new Error('登录响应缺少 token')
    }
    // 管理后台只对管理员开放：后端 /api/admin/** 要求 ADMIN 角色，
    // 普通用户在登录这一步就拦下，避免进入后台后每个请求都被拒又弹回登录页
    if (data.role !== 'ADMIN') {
      throw new Error('该账号不是管理员，无法登录管理后台')
    }
    // 更新响应式状态
    token.value = data.token
    userInfo.value = data
    // 持久化到 localStorage
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data))
    return data
  }

  /**
   * 获取最新用户信息（从后端拉取）
   * 用于页面初始化或 token 续期时同步最新数据
   */
  async function fetchUserInfo() {
    const res = await getInfoApi()
    userInfo.value = res
    localStorage.setItem('userInfo', JSON.stringify(res))
  }

  /**
   * 退出登录
   *
   * 先取出令牌，再清空本地登录态：
   * 本地状态要立即清掉，调用方才能马上跳转登录页；
   * 而登出请求需要带着原令牌才能让后端吊销它。
   * 请求不等待结果、失败也不抛出——本地已经登出，不应因为后端异常而卡住。
   */
  function logout() {
    const currentToken = token.value
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    if (currentToken) {
      logoutApi(currentToken).catch(() => {})
    }
  }

  // 导出状态和方法
  return { token, userInfo, login, fetchUserInfo, logout }
})
