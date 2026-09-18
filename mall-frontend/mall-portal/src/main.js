import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 样式引入顺序不能调换：
// 必须先引入 Element Plus 自带样式，再用设计令牌覆盖它的 --el-* 变量
import 'element-plus/dist/index.css'
import '@/styles/tokens.css'
import '@/styles/global.css'
import '@/styles/auth.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 全量注册 Element Plus 图标，模板中可直接使用 <el-icon><Search /></el-icon>
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
