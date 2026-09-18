# 智能商品管理系统

## 项目概述

基于 **Spring Boot 4.1.1 + Spring AI 2.0.1 + Python + Vue 3** 的全栈虚拟商品管理系统，集成 AI 智能客服和数据分析功能。采用「1 后端 + 1 数据分析服务 + 2 前端」架构，适合数据科学与大数据技术专业毕业设计。

> 商城对外名称为「**严选商城**」，管理后台页面标题为「**商城管理后台**」。

## 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 4.1.1 |
| **AI 框架** | Spring AI（通义千问） | 2.0.1 |
| **数据分析** | Python + FastAPI + pandas | 3.14 / 0.141 / 3.0 |
| **ORM** | MyBatis-Plus | 3.5.16 |
| **认证** | Spring Security + JWT (JJWT) | 0.12.6 |
| **数据库** | MySQL | 8.x |
| **前端框架** | Vue 3 + Vite 6 | 3.4+ |
| **UI 组件** | Element Plus | 2.7+ |
| **可视化** | ECharts | 5.5+ |
| **状态管理** | Pinia | 2.x |
| **构建工具** | Maven（后端）/ pip（Python）/ npm（前端） | — |

## 项目结构

```
SpringAiMall/
├── mall-server/              # 后端（Maven 多模块，父 POM 在本目录）
│   ├── mall-common/          # 公共模块：实体、Mapper、工具、安全、ORM 配置
│   ├── mall-ai/              # AI 客服模块：Spring AI + Function Calling
│   └── mall-admin/           # 统一后端服务：管理端 API + 用户端 API（启动入口）
├── mall-analytics/           # 数据分析服务（Python + FastAPI）
│   ├── app.py                # FastAPI 主应用（:8000）
│   ├── analyzer.py           # 销售分析 / 用户分析 / 商品推荐
│   ├── database.py           # 数据库连接配置
│   └── .env                  # 数据库连接覆盖（可选）
├── mall-frontend/            # 前端（两个独立项目，各自 npm install）
│   ├── mall-admin/           # 管理后台（Vue 3 + Element Plus + ECharts，:3000）
│   └── mall-portal/          # 用户商城（Vue 3 + Element Plus，:3001）
├── data/                     # AI 知识库向量库落盘目录（vector-store.json + 同名 .sha256 指纹）
└── uploads/images/           # 商品封面图（由后端 /api/uploads/** 提供访问）
```

## 架构图

```
┌─────────────────┐    ┌─────────────────┐
│   mall-admin    │    │   mall-portal   │
│  Vue3 管理后台   │    │  Vue3 用户商城   │
│  :3000          │    │  :3001          │
└────────┬────────┘    └────────┬────────┘
         │ /api 代理            │ /api 代理
         ▼                      ▼
┌─────────────────────────────────────────┐
│         mall-server (:8080)             │
│  Spring Boot 4.1.1 + Spring AI 2.0.1  │
│                                         │
│  com.yking.malladmin    → 管理端 API │
│  com.yking.mallportal   → 用户端 API │
│  com.yking.mallai       → AI 客服（通义千问）│
│  com.yking.mallcommon   → 实体/工具/安全 │
└──────────────┬──────────────────────────┘
               │ 调用 Python API
               ▼
┌─────────────────────────────────────────┐
│      mall-analytics (:8000)             │
│   Python + FastAPI + pandas             │
│                                         │
│  销售统计分析 → 用户价值分析            │
│  商品推荐     → 数据可视化              │
│                                         │
│         MySQL :3306/mall_db             │
└─────────────────────────────────────────┘
```

## 快速启动

### 1. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE mall_db DEFAULT CHARACTER SET utf8mb4;
```

数据库密码与 JWT 密钥通过环境变量传入，不写在配置文件里，启动前先设置：

```bash
export DB_PASSWORD=你的数据库密码                # Windows: set DB_PASSWORD=你的数据库密码
export JWT_SECRET=$(openssl rand -base64 32)   # 任意 32 字节随机值的 Base64 编码
```

其它连接信息（地址、库名、用户名）在 `mall-server/mall-admin/src/main/resources/application.yml` 中按需修改。

SQL 初始化脚本（都在 `mall-server/mall-admin/src/main/resources/` 下）：
- `schema.sql` — 建表（7 张表）
- `data.sql` — 初始数据（2 个账号种子 + 10 个分类 + 21 个商品）
- `mall_db.sql` — 完整库导出（建表 + 全部数据，需要一份能跑的演示数据时用它）

> ⚠️ 配置里 `spring.sql.init.mode: never`，这些脚本**不会自动执行**，需要手动导入：
> `mysql -uroot -p mall_db < schema.sql`

初始账号：
- 管理员：`admin` / `admin123`
- 测试用户：`user` / `user123`

### 2. 启动数据分析服务（Python）

```bash
cd mall-analytics
python -m venv .venv          # 首次才需要
.venv/Scripts/python.exe -m pip install -r requirements.txt
.venv/Scripts/python.exe app.py
```

> `.env` 里的 `DB_PASSWORD` 是占位值，改成你的数据库密码；也可以删掉该文件，
> 直接沿用上面设置的 `DB_PASSWORD` 环境变量。

服务启动在 `http://localhost:8000`  
API 文档：`http://localhost:8000/docs`

### 3. 启动后端（Java）

```bash
cd mall-server
mvn clean compile
# 主类：com.yking.malladmin.MallAdminApplication（位于 mall-admin 模块）
# 或：mvn -pl mall-admin -am spring-boot:run
#     （-am 表示一并构建它依赖的 mall-common、mall-ai；不加则首次运行会因解析不到这两个模块而失败）
```

后端启动在 `http://localhost:8080`

> AI 客服依赖的通义千问 Key 从**环境变量 `AI_API_KEY`** 读取（见 `application.yml`），
> 在 IDEA 的运行配置里配置它，否则 AI 对话会失败。

### 4. 启动管理后台前端

```bash
cd mall-frontend/mall-admin
npm install
npm run dev
```

访问 `http://localhost:3000`

### 5. 启动用户商城前端

```bash
cd mall-frontend/mall-portal
npm install
npm run dev
```

访问 `http://localhost:3001`

### 6. AI 客服配置（可选）

设置环境变量 `AI_API_KEY` 为通义千问 API Key，AI 客服功能才能正常工作。

AI 客服提供的能力：

- **SSE 流式对话** + 多轮会话管理（新建 / 切换 / 删除 / 清空），会话按用户隔离；
- **Function Calling**：查商品、热销榜、当前用户的订单与购物车，并可**代为取消订单**
  （取消动作与用户端「我的订单」共用同一套规则）；
- **RAG 知识库检索**：启动时把 `mall-ai` 模块的 `Consultation2025.pdf` 向量化，
  之后每次提问先检索相关片段再交给模型。

向量库落盘在 `data/vector-store.json`，旁边是同名的 `.sha256` 指纹文件——**只在首次运行
或 PDF 内容变化时才重新向量化**，之后启动直接读缓存，不再消耗嵌入模型额度。知识库初始化
失败不会阻止应用启动，只会打 WARN 并退化为普通对话。路径见 `application.yml` 的
`rag.vector-store-path`。

## API 路径规范

| 前缀 | 说明 | 权限 |
|------|------|------|
| `/api/auth/**` | 认证（登录/注册/退出） | 仅 `login`/`register`/`logout` 公开，`/api/auth/info` 需登录 |
| `/api/admin/**` | 管理端接口 | 需要 ADMIN 角色 |
| `/api/admin/analytics/**` | 数据分析接口 | 需要 ADMIN 角色 |
| `/api/portal/**` | 用户端接口 | 需要登录（商品列表/详情、分类树公开） |
| `/api/portal/ai/**` | AI 客服接口 | 需要登录 |
| `/api/uploads/**` | 商品封面图等静态资源 | 公开 |

## 数据分析功能

> 统计口径：**销量**取 `pms_product.sales_count`（与用户端「已售 N 件」同源）；
> **销售额**按「累计销量 × 现价」估算；**按时间切的指标**（每日趋势、销售汇总）
> 只统计已支付订单——待支付未成交、已取消要退款，都不计入。
> **分类筛选**：只有商品推荐会把所选父分类展开到全部子分类，分类销售额统计仍是精确匹配。

### 1. 销售统计分析
- Top10 热销商品排行
- 各分类销售额统计
- 近 N 天每日销售趋势
- 销售汇总数据（订单数、销售额、客单价）

### 2. 用户价值分析
- 用户分类（高价值/普通/低活跃）
- 用户消费统计（总消费、订单数、平均客单价）
- Top20 高价值用户列表

### 3. 商品推荐
- 全局热门商品 Top20
- 分类热门商品推荐
- 用户个性化推荐

### 数据分析 API 接口

```http
GET http://localhost:8000/api/analytics/sales?days=7         # 销售统计
GET http://localhost:8000/api/analytics/users                # 用户分析
GET http://localhost:8000/api/analytics/recommend            # 商品推荐
GET http://localhost:8000/api/analytics/recommend/user/{id}  # 单个用户的个性化推荐
GET http://localhost:8000/api/health                         # 健康检查
```

## 数据库表说明

| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表（管理员 + 普通用户） |
| `pms_category` | 商品分类（树形结构） |
| `pms_product` | 商品信息 |
| `oms_cart_item` | 购物车 |
| `oms_order` | 订单 |
| `oms_order_item` | 订单明细 |
| `ai_chat_message` | AI 对话记录 |

## 全局规范

- **所有代码注释使用中文**
- **所有对话使用中文交流**
- Java 版本：17+
- 编码：UTF-8

## 各模块详细文档

**后端**

- [mall-server（后端总览）](mall-server/README.md) —— 模块划分、依赖关系、版本管理
- [mall-common（公共模块）](mall-server/mall-common/README.md) —— 实体、Mapper、配置、工具
- [mall-ai（AI 客服模块）](mall-server/mall-ai/README.md) —— SSE 协议、Function Calling 工具（含代取消订单）
- [mall-admin（统一后端服务）](mall-server/mall-admin/README.md) —— 包结构、接口清单、业务流程

**数据分析**

- [mall-analytics（数据分析服务）](mall-analytics/README.md) —— 分析接口与统计口径

**前端**

- [mall-frontend（前端项目）](mall-frontend/README.md) —— 两个前端的关系与共同约定
- [mall-admin（管理后台前端）](mall-frontend/mall-admin/README.md)
- [mall-portal（用户商城前端）](mall-frontend/mall-portal/README.md)