# mall-admin — 统一后端服务

## 概述

**项目的唯一后端服务**（可执行 Fat JAR），同时提供管理端和用户端 API。聚合 `mall-common` 和 `mall-ai` 模块。

> ⚠️ 虽然目录名叫 `mall-admin`，但它是**统一后端**：管理端接口在 `com.yking.malladmin.*`，
> 用户端接口在 `com.yking.mallportal.*`。

## 启动入口

```
主类：com.yking.malladmin.MallAdminApplication
端口：8080
```

## 包结构

```
com.yking.malladmin
├── MallAdminApplication              # 启动类
│
├── controller/                       # ===== 管理端 =====
│   ├── AuthController                # 登录 / 注册 / 用户信息 / 退出
│   ├── AdminProductController        # 商品管理 CRUD + 上下架
│   ├── AdminCategoryController       # 分类管理 CRUD
│   ├── AdminOrderController          # 订单列表 / 统计 / 详情
│   ├── AdminUserController           # 用户管理
│   ├── AdminDashboardController      # 仪表盘统计
│   ├── AdminUploadController         # 图片上传
│   └── AdminAnalyticsController      # 转发 mall-analytics 的数据分析接口
├── service/                          # 管理端业务逻辑（含 impl）
└── dto/                              # 管理端出入参

com.yking.mallportal              # ===== 用户端 =====
├── controller/
│   ├── PortalProductController       # 商品浏览（列表/详情/分类树）
│   ├── CartController                # 购物车（增删改查）
│   └── OrderController               # 订单（下单/立即购买/支付/取消/列表）
├── dto/                              # 用户端出入参
└── service/                          # 用户端业务逻辑（含 impl）

com.yking.mallai                  # ===== AI 客服（mall-ai 模块）=====
├── controller/AiChatController       # 对话（SSE 流式）+ 会话历史
├── service/                          # 对话服务（含 impl）
├── function/                         # Function Calling 工具
│   ├── ProductFunction               # 商品查询、热销榜
│   ├── OrderFunction                 # 当前用户的历史订单 + 按订单号取消订单
│   └── CartFunction                  # 当前用户的购物车
└── config/RagConfig                  # PDF 知识库向量化 + 检索增强 advisor

com.yking.mallcommon              # ===== 公共模块 =====
├── entity/                           # 7 张表对应实体
├── mapper/                           # 全部 Mapper 接口（三模块共用）
├── service/                          # 跨模块共用的业务逻辑（订单取消）
├── config/                           # 安全、JWT 过滤器、MyBatis-Plus、静态资源
├── security/TokenBlacklist           # 退出登录后的令牌吊销集合
└── util/                             # JwtUtil / AuthUtil / DateTimeUtil
```

## API 接口清单

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册（公开） |
| POST | `/api/auth/login` | 用户登录（公开） |
| POST | `/api/auth/logout` | 退出登录（公开） |
| GET | `/api/auth/info` | 获取当前用户信息（需登录） |

### 管理端接口（需 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard` | 仪表盘统计数据 |
| GET | `/api/admin/products` | 商品列表（分页） |
| GET | `/api/admin/products/{id}` | 商品详情 |
| POST | `/api/admin/products` | 新增商品 |
| PUT | `/api/admin/products/{id}` | 编辑商品 |
| DELETE | `/api/admin/products/{id}` | 删除商品 |
| PUT | `/api/admin/products/{id}/status` | 上下架切换 |
| GET | `/api/admin/categories` | 分类树 |
| POST | `/api/admin/categories` | 新增分类 |
| PUT | `/api/admin/categories/{id}` | 编辑分类 |
| DELETE | `/api/admin/categories/{id}` | 删除分类 |
| GET | `/api/admin/orders` | 订单列表 |
| GET | `/api/admin/orders/stats` | 订单统计汇总（各状态数量 + 实收金额） |
| GET | `/api/admin/orders/{id}` | 订单详情 |
| GET | `/api/admin/users` | 用户列表 |
| PUT | `/api/admin/users/{id}/status` | 启用/禁用用户 |
| POST | `/api/admin/upload/image` | 上传图片（存到 `file.upload-dir` 下的 `images/`） |

### 数据分析接口（需 ADMIN 角色，转发 mall-analytics）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/analytics/sales` | 销售统计（Top 商品 / 分类统计 / 每日趋势 / 汇总） |
| GET | `/api/admin/analytics/users` | 用户价值分析（RFM） |
| GET | `/api/admin/analytics/recommend` | 热门商品推荐（可按分类） |
| GET | `/api/admin/analytics/recommend/user/{userId}` | 针对某个用户的推荐 |
| GET | `/api/admin/analytics/health` | 分析服务健康检查 |

### 用户端接口（需登录，部分公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/portal/products` | 商品列表（公开） |
| GET | `/api/portal/products/{id}` | 商品详情（公开） |
| GET | `/api/portal/categories` | 分类树（公开） |
| GET | `/api/portal/cart` | 购物车列表 |
| POST | `/api/portal/cart/add` | 添加购物车 |
| PUT | `/api/portal/cart/update` | 更新购物车 |
| DELETE | `/api/portal/cart/delete/{id}` | 删除购物车项 |
| POST | `/api/portal/orders` | 创建订单（把购物车中已勾选的商品下单） |
| POST | `/api/portal/orders/direct` | 立即购买：直接下单并支付，不经过购物车 |
| POST | `/api/portal/orders/{id}/pay` | 支付订单（模拟支付） |
| GET | `/api/portal/orders` | 我的订单列表（每笔含商品明细，含封面图与简介） |
| POST | `/api/portal/orders/{id}/cancel` | 取消订单 |
| POST | `/api/portal/ai/chat` | AI 对话（SSE 流式） |
| GET | `/api/portal/ai/sessions` | 我的会话列表 |
| GET | `/api/portal/ai/sessions/{sessionId}/messages` | 某个会话的历史消息 |
| DELETE | `/api/portal/ai/sessions/{sessionId}` | 删除某个会话 |
| DELETE | `/api/portal/ai/sessions` | 清空我的全部会话 |

> 商品列表接口 `/api/portal/products` 支持 `keyword`（名称模糊）、`categoryId`、
> `sort=sales`（按销量倒序，供首页热销榜使用）。

## 核心业务流程

### 下单流程（购物车）

```
用户勾选商品 → 提交订单
                  ↓
   校验库存 → 扣减库存 → 生成订单 → 创建订单明细 → 清空已勾选的购物车项
```

### 立即购买流程（不走购物车）

```
选择数量 → 下单并支付
              ↓
   校验商品 → 扣减库存 → 生成订单 → 创建明细 → 直接置为已支付、累加销量
```

### 支付流程

```
用户支付 → pay_status=1 → 记录支付时间 → 按明细数量累加商品销量
```

### 取消订单

```
用户取消 → pay_status=2（先判断当前不是已取消，避免重复操作）
        → 恢复商品库存
        → 若原本已支付，再按明细数量把销量扣回（GREATEST 兜底，不会扣成负数）
```

取消规则实现在**公共模块的 `OrderCancelService`**：用户端「我的订单」里的取消按钮、
AI 客服的「取消订单」工具都调它，保证两处行为完全一致。
AI 那条路径还多一层限制——只能按「订单号 + 当前用户」查单，查不到就拒绝。

### 销量与销售额的口径

| 指标 | 口径 |
|------|------|
| 商品销量 `pms_product.sales_count` | 支付时按明细数量累加，取消已支付订单时扣回。用户端「已售 N 件」与管理端商品列表读的都是它 |
| 累计销售额（仪表盘） | 按「商品累计销量 × 现价」估算，与 mall-analytics 商品/分类维度同口径 |
| 按时间切的销售额（今日、近 7 天趋势、订单统计） | 只统计**已支付**订单：待支付未成交、已取消要退款，都不计入 |

## Mapper 说明

所有 Mapper 统一放在 **mall-common** 模块的 `com.yking.mallcommon.mapper` 包中，三个模块共用：

| Mapper | 对应实体 |
|--------|----------|
| `PmsProductMapper` | PmsProduct（商品） |
| `PmsCategoryMapper` | PmsCategory（分类） |
| `OmsOrderMapper` | OmsOrder（订单） |
| `OmsOrderItemMapper` | OmsOrderItem（订单明细） |
| `OmsCartItemMapper` | OmsCartItem（购物车） |
| `SysUserMapper` | SysUser（用户） |
| `AiChatMessageMapper` | AiChatMessage（AI 对话记录） |

## 关键依赖

```xml
mall-common    <!-- 实体/工具/安全/ORM -->
mall-ai        <!-- AI 客服 -->
```

## 配置文件与 SQL 脚本

`src/main/resources/application.yml` — 唯一配置文件，包含数据库、JWT、AI、MyBatis-Plus、
文件上传目录、Python 分析服务地址等所有配置。

| 文件 | 说明 |
|------|------|
| `schema.sql` | 建表 SQL（7 张表） |
| `data.sql` | 初始化数据（分类、示例商品） |
| `mall_db.sql` | 完整库导出（建表 + 全部数据） |

> `spring.sql.init.mode: never` —— 这些脚本不会随启动自动执行，需要手动导入。
