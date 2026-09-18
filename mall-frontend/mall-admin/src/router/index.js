/**
 * 管理后台路由配置
 *
 * 路由结构：
 *   /login          → 登录页（独立布局）
 *   /               → 后台主布局（含侧边栏 + 顶栏），包含以下子路由：
 *     /dashboard    → 仪表盘（数据统计概览）
 *     /products     → 商品列表
 *     /products/edit      → 新增商品
 *     /products/edit/:id  → 编辑商品
 *     /categories   → 分类管理
 *     /orders       → 订单管理
 *     /users        → 用户管理
 *
 * 使用嵌套路由 + Layout 组件实现统一后台布局
 */
import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由表
 *
 * meta.title: 页面标题（可用于浏览器 tab 标题、面包屑等）
 */
const routes = [
  // ===== 登录页（独立布局，不在 Layout 内）=====
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },

  // ===== 后台主布局（包含侧边栏导航 + 顶部栏 + 内容区）=====
  {
    path: '/',
    component: () => import('@/layout/index.vue'),  // 后台统一布局组件
    redirect: '/dashboard',                          // 默认跳转到仪表盘
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/product/list.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'products/edit',
        name: 'ProductAdd',
        component: () => import('@/views/product/edit.vue'),
        meta: { title: '新增商品' }
      },
      {
        path: 'products/edit/:id',                   // :id 为商品ID，编辑时传入
        name: 'ProductEdit',
        component: () => import('@/views/product/edit.vue'),
        meta: { title: '编辑商品' }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'analytics',
        name: 'Analytics',
        component: () => import('@/views/analytics/index.vue'),
        meta: { title: '数据分析' }
      }
    ]
  }
]

// 创建路由实例，使用 HTML5 History 模式（URL 无 # 号）
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局前置导航守卫
 *
 * 逻辑：
 *   1. 访问非登录页 + 登录态无效 → 清除本地登录信息并跳转登录页
 *      （登录态有效 = 有 token 且角色为 ADMIN，与后端 /api/admin/** 的权限要求一致）
 *   2. 访问登录页 + 登录态有效 → 直接跳转到仪表盘（避免重复登录）
 *   3. 其他情况 → 正常放行
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  // 仅凭 token 存在不足以放行：普通用户登录过之后残留的 token 会被后端判 403
  const isLoggedIn = Boolean(token) && userInfo.role === 'ADMIN'

  if (to.path !== '/login' && !isLoggedIn) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    next('/login')
  } else if (to.path === '/login' && isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
