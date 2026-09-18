/**
 * 购物车角标
 *
 * 顶栏需要显示购物车内的商品总件数。任何会改变购物车的操作
 * （加购、改数量、删除、下单）之后都应调用 refresh() 重新拉取。
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCart } from '@/api/cart'
import { TOKEN_KEY } from '@/utils/request'

export const useCartStore = defineStore('cart', () => {
  /** 购物车商品总件数 */
  const count = ref(0)

  /**
   * 重新拉取购物车并更新角标
   *
   * 未登录时直接归零：购物车接口需要登录，贸然请求会得到 403，
   * 而请求拦截器会把 403 当成会话失效，把用户弹到登录页。
   */
  async function refresh() {
    if (!localStorage.getItem(TOKEN_KEY)) {
      count.value = 0
      return
    }
    try {
      const items = await getCart()
      count.value = items.reduce((sum, item) => sum + item.quantity, 0)
    } catch (e) {
      count.value = 0
    }
  }

  /** 登出时归零 */
  function clear() {
    count.value = 0
  }

  return { count, refresh, clear }
})
