/**
 * 用户登录态
 *
 * 令牌与用户信息持久化在 localStorage，刷新页面后保持登录。
 * 键名统一由 utils/request.js 导出，避免各处硬编码字符串。
 */
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { TOKEN_KEY, USER_KEY } from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || '{}'))

  /** 是否已登录 */
  const isLoggedIn = computed(() => Boolean(token.value))

  /** 顶栏展示用的称呼 */
  const displayName = computed(() => userInfo.value.nickname || userInfo.value.username || '用户')

  /**
   * 登录
   * @param {Object} form { username, password }
   */
  async function login(form) {
    const data = await loginApi(form)
    if (!data?.token) {
      throw new Error('登录响应缺少 token')
    }
    token.value = data.token
    userInfo.value = data
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(data))
    return data
  }

  /**
   * 退出登录
   *
   * 先取出令牌再清空本地登录态：登出请求需要带着原令牌才能让后端吊销它，
   * 而本地状态要立即清掉，调用方才能马上跳转。
   * 请求不等待结果、失败也不抛出，本地已登出不应因后端异常而卡住。
   */
  function logout() {
    const currentToken = token.value
    clearLocal()
    if (currentToken) {
      logoutApi(currentToken).catch(() => {})
    }
  }

  /** 清空本地登录态 */
  function clearLocal() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, userInfo, isLoggedIn, displayName, login, logout }
})
