import { defineConfig } from 'vite'      // Vite 配置函数
import vue from '@vitejs/plugin-vue'     // Vue 3 单文件组件支持插件
import { resolve } from 'path'

export default defineConfig({
    // ==================== 插件 ====================
    plugins: [vue()],

    // ==================== 路径别名 ====================
    resolve: {
        alias: {
            // 将 @ 映射到 src 目录，方便写 import Xxx from '@/components/Xxx'
            '@': resolve(__dirname, 'src')
        }
    },

    // ==================== 开发服务器 ====================
    server: {
        // 用户端商城端口，与管理后台（3000）区分开
        port: 3001,

        // 接口代理：将前端 /api 开头的请求转发到后端，解决跨域问题
        proxy: {
            '/api': {
                target: 'http://localhost:8080',  // 后端服务地址
                changeOrigin: true                 // 修改请求头 Origin 为目标地址，避免后端 CORS 拒绝
            }
        }
    }
})
