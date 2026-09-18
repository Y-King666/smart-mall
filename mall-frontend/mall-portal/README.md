# mall-portal — 用户商城前端

## 概述

基于 **Vue 3 + Element Plus** 的用户端商城，支持 PC 和移动端响应式布局。功能包括商品浏览、购物车、下单支付、订单管理、AI 智能客服。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4+ | 前端框架（Composition API） |
| Vite | 6.0+ | 构建工具 |
| Element Plus | 2.7+ | UI 组件库 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP 请求 |

> 📌 与管理后台相比，本模块**不含 ECharts**（不需要图表），有一套自己的设计令牌
> （`styles/tokens.css`）与通用样式类，并按 `max-width: 900px / 520px` 做响应式适配。

## 目录结构

```
mall-portal/
├── index.html                    # 入口 HTML
├── package.json                  # 依赖配置
├── vite.config.js                # Vite 配置（端口 3001）
├── .env.development              # 开发环境变量（接口基地址）
├── public/logo.png               # 站标（同时作为浏览器标签图标）
└── src/
    ├── App.vue                   # 根组件
    ├── main.js                   # 入口文件
    ├── api/                      # API 接口层
    │   ├── auth.js               # 登录 / 注册
    │   ├── product.js            # 商品浏览
    │   ├── cart.js               # 购物车
    │   ├── order.js              # 订单（含立即购买）
    │   └── ai.js                 # AI 客服（SSE 流式）
    ├── components/               # 公共组件
    │   ├── RecommendCarousel.vue # 首页热销层叠轮播
    │   ├── QuantityStepper.vue   # 数量增减控件
    │   ├── QuantityDialog.vue    # 选数量弹层（加购 / 立即购买共用）
    │   ├── ProductSuggest.vue    # 推荐商品（列表版式 / 卡片版式两用）
    │   ├── AiChatPanel.vue       # AI 对话面板（整页与悬浮窗共用）
    │   └── AiAssistantWidget.vue # 右下角 AI 客服悬浮球与悬浮窗
    ├── layout/
    │   └── index.vue             # 整体布局（顶栏 + 内容区 + 页脚 + 悬浮窗）
    ├── router/
    │   └── index.js              # 路由配置（含登录守卫）
    ├── store/
    │   ├── user.js               # 用户状态（token、用户信息、登录/登出）
    │   └── cart.js               # 购物车状态（列表/数量角标）
    ├── styles/                   # 设计令牌与全局样式
    │   ├── tokens.css            # 设计令牌 + Element Plus 主题映射
    │   ├── global.css            # 通用类（mp-container / mp-btn / mp-price 等）
    │   └── auth.css              # 登录注册共用版式
    ├── utils/                    # 工具
    │   ├── request.js            # Axios 封装
    │   ├── product.js            # 金额格式化、折扣判断、立省金额
    │   └── order.js              # 订单状态文案与可操作性判断
    └── views/                    # 页面视图
        ├── home/index.vue        # 商品页（热销轮播/分类筛选/商品网格）
        ├── login/index.vue       # 登录页
        ├── register/index.vue    # 注册页
        ├── product/detail.vue    # 商品详情（主图/规格/加购/立即购买/猜你喜欢）
        ├── cart/index.vue        # 购物车（勾选/数量/结算）
        ├── checkout/index.vue    # 结算确认页
        ├── order/list.vue        # 我的订单（一笔订单一卡，卡内含商品明细）
        └── ai/index.vue          # 智能客服整页（会话侧栏 + 对话区 + 推荐栏）
```

## 启动与构建

```bash
# 安装依赖
npm install

# 开发模式
npm run dev
# → http://localhost:3001

# 生产构建
npm run build

# 预览
npm run preview
```

## 请求流程

```
Vue 页面 → api/*.js → utils/request.js（Axios）
  baseURL = VITE_API_BASE_URL（.env.development 里为 http://localhost:8080）
  → 直连后端 :8080
```

> `vite.config.js` 里也配了 `/api` 代理，但只有把 `VITE_API_BASE_URL` 留空时才会走它；
> 当前配置是直连，浏览器网络面板里看到的请求地址就是 `http://localhost:8080/api/...`。

### Axios 拦截器

**请求拦截器**：自动附加 JWT Token

**响应拦截器**：
- 解包 `Result<T>` → 返回内层 `data`
- 401 → 清除登录态，跳转登录页

> AI 聊天的 SSE 接口**不走 Axios**：`api/ai.js` 用原生 `fetch` + `ReadableStream` 手工解析事件流，
> 所以拦截器里没有 SSE 相关处理。

> ⚠️ **重要**：拦截器已解包，页面直接使用 `res.records` 等，不要再写 `res.data.records`。

## 路由说明

| 路径 | 页面 | 需要登录 |
|------|------|---------|
| `/` | 重定向到 `/products` | 否 |
| `/products` | 商品页（热销轮播 + 分类筛选 + 商品网格） | 否 |
| `/products/:id` | 商品详情 | 否 |
| `/cart` | 购物车 | ✅ |
| `/checkout` | 结算页 | ✅ |
| `/orders` | 我的订单 | ✅ |
| `/ai` | 智能客服 | ✅ |
| `/login` | 登录 | 否 |
| `/register` | 注册 | 否 |

**导航守卫**：`meta.requiresAuth` 的路由未登录时 → 跳转 `/login?redirect=原路径`

## 核心功能

### 商品浏览
- 商品页：热销轮播 + 分类筛选 + 商品网格（站点落地页，地址 `/products`）
- 商品详情：主图、价格与划线原价、规格表、加购物车 / 立即购买、猜你喜欢
- 支持关键字搜索与分类筛选，筛选条件全部写进地址栏（刷新、分享都能还原）
- 顶栏：左侧导航（商品 / 我的订单）+ 关键字搜索框；登录后右侧下拉菜单含「我的订单」与「退出登录」

### 购物车
- 添加商品（自动合并相同商品数量）
- 修改数量 / 勾选状态、删除商品项
- 实时计算已勾选商品总价

### 下单支付
- 购物车下单：勾选商品 → 结算确认页 → 创建订单
- 立即购买：详情页选数量后直接下单并支付，不经过购物车
- 创建订单时：校验库存 → 扣减库存 → 生成订单 → 清空已购购物车项
- 模拟支付：更新订单状态为已支付，并累加商品销量
- 取消订单：恢复库存；若原本已支付，同时把销量扣回

### 订单管理
- 订单列表：一笔订单一卡，卡内直接展示这笔买了什么（缩略图、名称、简介、单价、数量）
- 卡内可支付、取消

### AI 智能客服
- 两个入口：右下角悬浮窗（`AiAssistantWidget.vue`）、整页 `/ai`（会话侧栏 + 对话区 + 推荐栏）
- 两者共用同一个对话面板组件（`AiChatPanel.vue`），整页是完整版、悬浮窗是紧凑版
- SSE 流式对话（逐字输出），支持多轮对话与会话历史（新建 / 切换 / 删除 / 清空）
- AI 可自动查询商品、热销榜、当前用户的订单与购物车（Function Calling）
- AI 还能**代为取消订单**：用户说要取消时会先问清订单号，拿到后才执行；只能取消自己的订单
- 未登录时点开悬浮窗只提示去登录，不直接跳转

### 响应式布局
- 顶栏在窄屏折行，商品网格与详情页改为单列
- 断点：`max-width: 900px`（单列）、`max-width: 520px`（悬浮窗铺满宽度）

## Store 说明

### user store

```javascript
// 状态
token              // JWT，持久化在 localStorage
userInfo           // 用户信息对象

// 计算属性
isLoggedIn         // 是否已登录
displayName        // 昵称，取不到时退回用户名

// 方法
login({ username, password })   // 登录（同时写入本地存储）
logout()                        // 退出（调后端接口 + 清本地状态）

// clearLocal()（只清本地登录态）是内部函数，未对外导出，由 logout() 调用
```

### cart store

购物车数据本身不在前端存：状态里只保留**数量角标**，
加购、改数量、删除、下单之后都应调用 `refresh()` 重新拉取。

```javascript
count        // 购物车商品总件数（顶栏角标）
refresh()    // 从服务端重新统计件数
clear()      // 清空本地计数（退出登录时调用）
```

## 公共组件

### RecommendCarousel — 首页热销轮播
层叠式大卡片轮播，取热销榜前 10 件依次轮转（后端 `sort=sales` 排序），
每次露出 3 张，点击正中卡片进详情。

### QuantityStepper — 数量增减
商品详情与选数量弹层共用的 −/+ 控件，带最小/最大值约束。

### QuantityDialog — 选数量弹层
「加入购物车」与「立即购买」共用的弹层：选好数量后按 `mode` 决定是加购（去购物车）
还是直接下单支付（去订单列表）。

### ProductSuggest — 推荐商品
「猜你喜欢 / 智能客服推荐」共用一个组件，两种版式：
窄栏用一行一件的列表版式，宽栏用多列卡片版式；取数规则是**同分类优先、热销补齐**。

### AiChatPanel — AI 对话面板
整页与悬浮窗共用的对话面板，只通过 `compact` 属性切换尺寸密度。
负责展示历史、发送、流式接收三件事。

### AiAssistantWidget — AI 客服悬浮窗
右下角悬浮球 + 展开的小对话窗，进入 `/ai` 整页时自动隐藏。

会话标识存在 `sessionStorage`：**刷新页面、站内跳转都不会丢聊天记录**，关掉标签页才重新开始——
即把「一次完整的浏览」视为一个会话；点「新对话」则立刻换一个会话。

## 环境变量

`.env.development`：
```
VITE_API_BASE_URL=http://localhost:8080
```

## 开发规范

- 使用 `<script setup>` 语法（Composition API）
- 组件命名：PascalCase；API 函数命名：camelCase
- 所有注释使用中文
- 样式使用 `<style scoped>`，通用样式类与设计令牌放在 `styles/` 下
- **商品封面统一 1:1**：所有展示位用 `aspect-ratio: 1/1`（或等宽高）+ `object-fit: cover`，
  新增图片位照此办理
