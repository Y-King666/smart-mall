/**
 * 用户端商城路由配置
 *
 * 使用 HTML5 History 模式（URL 无 # 号）。
 * 登录、注册是独立页面；其余页面都套在带顶栏与页脚的 layout 之下，
 * 后续新增的购物车、结算、订单页直接加进 children 即可。
 */
import { createRouter, createWebHistory } from 'vue-router'
import { TOKEN_KEY } from '@/utils/request'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        // 商品页在 /products，根路径保留重定向：站内 logo、老链接、外部分享的地址都不会失效
        path: '',
        redirect: '/products'
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '商品' }
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/product/detail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/cart/index.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/checkout/index.vue'),
        meta: { title: '确认订单', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/order/list.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'ai',
        name: 'AiAssistant',
        component: () => import('@/views/ai/index.vue'),
        meta: { title: '智能客服', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  // 切换路由时回到页面顶部，避免从长列表进入详情页时停留在原滚动位置
  scrollBehavior: () => ({ top: 0 })
})

/**
 * 登录守卫
 *
 * 购物车、结算这类页面直接输入网址进来时也要拦住，
 * 不能只依赖顶栏按钮的跳转判断
 */
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem(TOKEN_KEY)) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

// 用路由的标题同步浏览器标签页标题
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 严选商城` : '严选商城'
})

export default router
