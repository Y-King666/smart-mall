# mall-frontend — 前端项目

## 概述

用户端前台与管理后台两个 Vue 3 项目。两者**各自独立**：各自有 `package.json`，
需要分别 `npm install`、分别启动（**不是 npm workspaces**）。

| 项目 | 端口 | 访问者 | 技术栈 | 文档 |
|------|------|--------|--------|------|
| `mall-admin` | 3000 | 管理员 | Vue 3 + Element Plus + ECharts | [README](mall-admin/README.md) |
| `mall-portal` | 3001 | 顾客 | Vue 3 + Element Plus（自带设计令牌） | [README](mall-portal/README.md) |

## 启动

```bash
# 管理后台
cd mall-admin && npm install && npm run dev     # → http://localhost:3000

# 用户商城
cd mall-portal && npm install && npm run dev    # → http://localhost:3001
```

两个项目都通过 `.env.development` 里的 `VITE_API_BASE_URL` 直连后端
（默认 `http://localhost:8080`）。

## 共同约定

- 使用 `<script setup>` 语法（Composition API）
- 组件命名 PascalCase，API 函数命名 camelCase
- 所有注释使用中文；组件样式一律用 `<style scoped>`（全局重置这类少数情况才用非 scoped 的
  `<style>`，目前仅 `mall-admin/src/App.vue` 一处）
- 两个项目都配了 `@` → `src` 的路径别名（各自的 `vite.config.js`），import 一律写 `@/...`
- 接口响应统一被 `utils/request.js` 的拦截器解包，页面里直接用 `res.records` 这类字段，
  **不要再写 `res.data.records`**
- 商品封面统一 **1:1**：所有图片位用 `aspect-ratio: 1/1`（或等宽高）+ `object-fit: cover`
