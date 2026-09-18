# mall-admin — 管理后台前端

> 浏览器标签页标题为「**商城管理后台**」。

## 概述

基于 **Vue 3 + Element Plus + ECharts** 的商品管理后台，用于管理员进行商品、分类、订单、用户管理和查看仪表盘数据。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4+ | 前端框架（Composition API） |
| Vite | 6.0+ | 构建工具 |
| Element Plus | 2.7+ | UI 组件库 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP 请求 |
| ECharts | 5.5+ | 图表（仪表盘销售趋势） |

## 目录结构

```
mall-admin/
├── index.html                    # 入口 HTML
├── package.json                  # 依赖配置
├── vite.config.js                # Vite 配置（端口 3000，代理 /api）
├── .env.development              # 开发环境变量
└── src/
    ├── App.vue                   # 根组件
    ├── main.js                   # 入口文件（注册 Element Plus、Pinia、Router）
    ├── api/                      # API 接口层
    │   ├── auth.js               # 登录/注册/获取用户信息
    │   ├── product.js            # 商品管理接口
    │   ├── category.js           # 分类管理接口
    │   ├── order.js              # 订单管理接口
    │   ├── user.js               # 用户管理接口
    │   ├── upload.js             # 图片上传
    │   ├── dashboard.js          # 仪表盘数据接口
    │   └── analytics.js          # 数据分析接口（转发 Python 服务）
    ├── layout/
    │   └── index.vue             # 页面布局框架（侧边栏 + 顶栏 + 内容区）
    ├── router/
    │   └── index.js              # 路由配置（含导航守卫）
    ├── store/
    │   └── user.js               # 用户状态（token、登录/登出）
    ├── utils/
    │   ├── request.js            # Axios 封装（拦截器、JWT、错误处理）
    │   └── category.js           # 分类树拍平 / 选项缩进（三处页面共用）
    └── views/                    # 页面视图
        ├── login/index.vue       # 登录页
        ├── dashboard/index.vue   # 仪表盘（统计卡片 + 销售趋势图）
        ├── product/
        │   ├── list.vue          # 商品列表（搜索/分页/上下架/删除）
        │   └── edit.vue          # 商品编辑（新增/编辑复用）
        ├── category/index.vue    # 分类管理（树形表格/新增/编辑/删除）
        ├── order/index.vue       # 订单管理（搜索/分页/详情弹窗）
        ├── user/index.vue        # 用户管理（搜索/分页/启用禁用）
        └── analytics/index.vue   # 数据分析（销售/用户分析/商品推荐 + 图表）
```

## 启动与构建

```bash
# 安装依赖
npm install

# 开发模式（热更新）
npm run dev
# → http://localhost:3000

# 生产构建
npm run build
# → dist/ 目录

# 预览生产构建
npm run preview
```

## 请求流程

```
Vue 页面 → api/*.js → utils/request.js（Axios）
  baseURL = VITE_API_BASE_URL（.env.development 里为 http://localhost:8080）
  → 直连后端 :8080
```

> `vite.config.js` 里也配了 `/api` 代理，但只有把 `VITE_API_BASE_URL` 留空时才会走它。

### Axios 拦截器

**请求拦截器**：自动附加 JWT Token 到 `Authorization: Bearer <token>`

**响应拦截器**：
- 自动解包 `Result<T>` → 返回内层 `data`
- `code === 200` → 返回 `res.data`
- `code === 401` → 清除 token，跳转登录页
- 其他错误 → `ElMessage.error()` 提示

> ⚠️ **重要**：因为拦截器已经解包，页面中直接使用 `res.records`、`res.total` 等，不要再写 `res.data.records`。

## 路由说明

| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | 登录页 | 无需认证 |
| `/` | 布局框架 | 重定向到 `/dashboard` |
| `/dashboard` | 仪表盘 | 统计卡片 + ECharts 图表 |
| `/products` | 商品列表 | 搜索/分页/状态切换 |
| `/products/edit` | 新增商品 | 表单页 |
| `/products/edit/:id` | 编辑商品 | 表单页（回填数据） |
| `/categories` | 分类管理 | 树形表格 |
| `/orders` | 订单管理 | 列表 + 详情弹窗 |
| `/users` | 用户管理 | 列表 + 状态切换 |
| `/analytics` | 数据分析 | 销售统计 / 用户分析 / 商品推荐三个页签 |

**导航守卫**：未登录访问非 `/login` 页面 → 自动跳转登录页

## 页面功能

### 仪表盘（Dashboard）
- 4 个统计卡片：商品总数、订单总数、总销售额、今日销售额
- ECharts 折线/柱状图：近 7 天销售趋势（只统计已支付订单）

### 商品管理
- 列表：封面图、名称、价格、库存、销量、状态开关
- 搜索：按名称/分类/状态筛选
- 分类下拉按层级缩进显示；选父分类表示「它下面所有子分类的商品」
- 编辑页的商品分类只能选**叶子分类**（父分类是分组，挂上去用户端按分类逛不到）
- 操作：编辑（跳转 edit 页）、删除（确认弹窗）、上下架（Switch 切换）

### 分类管理
- 树形表格展示（支持多级分类）
- 弹窗表单：名称、父分类选择、排序

### 订单管理
- 顶部 5 张统计卡片：订单总数、待支付、已支付、已取消、订单总额
- 列表：订单号、金额、状态标签、时间
- 搜索：按订单号/支付状态筛选
- 详情弹窗：订单信息 + 商品明细表格

### 用户管理
- 列表：用户名、昵称、角色标签、状态开关、注册时间
- 搜索：按用户名/昵称搜索

### 数据分析（Analytics）
- 销售分析：Top 商品排行、分类统计、每日趋势（可选近 7/30/90 天）、销售汇总
- 用户分析：RFM 用户价值分类、用户类型分布饼图、Top20 高价值用户列表
- 商品推荐：全局/按分类热门商品（分类下拉按层级缩进，选父分类含子分类）；
  高价值用户表格里点「查看推荐」可拉取单个用户的个性化推荐
- 数据由后端转发到独立的 Python 分析服务（页面只调 `/api/admin/analytics/*`），图表用 ECharts 渲染

## 环境变量

`.env.development`：
```
VITE_API_BASE_URL=http://localhost:8080
```

## 开发规范

- 使用 `<script setup>` 语法（Composition API）
- 组件命名：PascalCase
- API 函数命名：camelCase（如 `getProductList`、`toggleProductStatus`、`uploadImage`）
- 所有注释使用中文
