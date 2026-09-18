/**
 * 管理后台（mall-admin）应用入口
 *
 * 初始化流程：
 *   1. 创建 Vue 应用实例
 *   2. 注册 UI 框架 Element Plus（中文化 + 全套图标）
 *   3. 注册 Pinia 状态管理
 *   4. 注册 Vue Router 路由
 *   5. 挂载到 DOM 节点 #app
 *
 * 与 mall-portal 的区别：
 *   - 引入 Element Plus 中文语言包（zhCn），组件文案显示中文
 *   - Pinia 在 Router 之前注册（路由守卫中用到 userStore）
 */
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'                    // Element Plus 组件样式
import zhCn from 'element-plus/es/locale/lang/zh-cn'    // Element Plus 中文语言包
import * as ElementPlusIconsVue from '@element-plus/icons-vue'  // Element Plus 图标库
import { createPinia } from 'pinia'                     // 状态管理
import router from './router'                           // 路由配置
import App from './App.vue'                             // 根组件

// 创建 Vue 应用实例
const app = createApp(App)

/**
 * 全局注册所有 Element Plus 图标组件
 *
 * 注册后可在模板中直接使用图标名作为动态组件：
 *   <el-icon><User /></el-icon>
 *   <component :is="iconName" />
 */
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 按顺序注册插件
app.use(createPinia())                          // 状态管理（必须在 router 之前）
app.use(router)                                 // 路由
app.use(ElementPlus, { locale: zhCn })          // UI 组件库 + 中文国际化

// 挂载应用到页面 #app 节点
app.mount('#app')
