# mall-analytics — 数据分析服务

## 概述

独立的 Python 服务，用 FastAPI 对外提供销售统计、用户价值分析与商品推荐。
后端 `mall-admin` 的管理端接口 `/api/admin/analytics/**` 会把请求转发到这里，管理后台的
「数据分析」页面展示的就是本服务的返回结果。

- 端口：**8000**
- 数据库：与后端共用同一个 MySQL（`mall_db`），只读，不写任何数据
- 接口文档：`http://localhost:8000/docs`（FastAPI 自带 Swagger UI）

## 目录结构

```
mall-analytics/
├── app.py                # FastAPI 主应用：路由、参数校验、统一响应格式
├── analyzer.py           # 分析逻辑：SalesAnalyzer / UserAnalyzer / ProductRecommender
├── database.py           # 数据库连接（SQLAlchemy + PyMySQL）
├── requirements.txt      # 依赖
├── .env                  # 数据库连接覆盖（可选，见下方「启动」）
└── .venv/                # 虚拟环境（本地已包含依赖，勿提交）
```

## 启动

```bash
cd mall-analytics

# 首次：建虚拟环境并装依赖
python -m venv .venv
.venv/Scripts/python.exe -m pip install -r requirements.txt

# 启动
.venv/Scripts/python.exe app.py
```

数据库连接信息默认从环境变量读取（`DB_HOST` / `DB_PORT` / `DB_USER` / `DB_PASSWORD` / `DB_NAME`），
可以在同目录放一个 `.env` 覆盖，不配则用 `database.py` 里的默认值。

## 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 服务信息 |
| GET | `/api/health` | 健康检查 |
| GET | `/api/analytics/sales?days=7` | 销售统计：Top 商品、分类统计、每日趋势、汇总（`days` 1–365） |
| GET | `/api/analytics/users` | 用户价值分析（简化 RFM） |
| GET | `/api/analytics/recommend?category_id=&limit=20` | 热门商品推荐，可按分类过滤 |
| GET | `/api/analytics/recommend/user/{user_id}` | 针对某个用户的推荐（排除已购买过的） |

除根路由 `/`（返回服务名与接口列表）外，其余接口响应统一为
`{"code": 200, "message": "success", "data": {...}}`（`/api/health` 的 message 是「服务运行正常」）。

## 分析逻辑

| 类 | 方法 | 说明 |
|----|------|------|
| `SalesAnalyzer` | `get_top_products` | 热销商品排行（按销量倒序） |
| | `get_category_stats` | 各分类的商品数与销量、销售额 |
| | `get_daily_sales` | 近 N 天每日订单数、销售额、下单用户数 |
| | `get_sales_summary` | 近 N 天汇总（订单数、用户数、销售额、客单价） |
| `UserAnalyzer` | `analyze_user_value` | 按消费频次与金额给用户分级 |
| `ProductRecommender` | `get_hot_products` | 全局或按分类的热门商品 |
| | `get_recommendations_for_user` | 排除用户已购买过的商品后按热度推荐 |

## 统计口径

**这四条与后端 `mall-admin` 保持一致，改动时两边要一起改。**

| 指标 | 口径 |
|------|------|
| 销量 | 取 `pms_product.sales_count`（与用户端「已售 N 件」同源），**不从订单明细累加**——商品带有初始化销量，没有对应订单，从订单算会与用户端对不上 |
| 销售额（商品/分类维度） | 按「累计销量 × 现价」估算，与销量自洽 |
| 按时间切的指标（每日趋势、销售汇总、用户消费统计） | 只统计**已支付**订单：待支付未成交、已取消要退款，都不计入 |
| 分类筛选（仅 `/recommend` 热门商品） | 选父分类时包含其全部子分类（商品挂在叶子分类上，只精确匹配会查不出结果）。`/api/analytics/sales` 的**分类销售额统计仍按商品所属分类精确匹配，不聚合子分类** |

## 注意

- 本服务**只读数据库**，所有分析结果实时计算，不做缓存。
- 后端调用本服务的地址配在 `mall-server/mall-admin` 的 `application.yml` 的
  `analytics.base-url`（默认 `http://localhost:8000`）。本服务没启动时，
  管理后台的「数据分析」页面会提示服务不可用。
