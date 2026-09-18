"""
FastAPI 主应用
提供数据分析 REST API 接口
"""
# FastAPI 是 Python 现代 Web 框架，性能媲美 Node.js/Go，自动生成 API 文档
# FastAPI: 创建应用实例，类似 Spring Boot 的 @SpringBootApplication
# Query: 用于定义查询参数的验证规则
from fastapi import FastAPI, Query
# CORSMiddleware: 跨域资源共享中间件，允许前端（不同端口）访问后端 API
from fastapi.middleware.cors import CORSMiddleware
# 从 analyzer 模块导入三个分析器类，用于处理具体的业务逻辑
from analyzer import SalesAnalyzer, UserAnalyzer, ProductRecommender

# 创建 FastAPI 应用实例（类似 Spring Boot 的 SpringApplication）
# title/description/version 会显示在自动生成的 Swagger 文档中
app = FastAPI(
    title="商城数据分析 API",
    description="基于 Python 的商城数据分析服务，提供销售统计、用户分析、商品推荐等功能",
    version="1.0.0"
)

# 配置 CORS 中间件，解决前后端分离时的跨域问题
# 类似 Spring 的 @CrossOrigin 注解，但这里是全局配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 允许所有来源，生产环境应该指定具体的域名（如 ["http://localhost:5173"]）
    allow_credentials=True,  # 允许携带 Cookie
    allow_methods=["*"],     # 允许所有 HTTP 方法（GET/POST/PUT/DELETE 等）
    allow_headers=["*"],     # 允许所有请求头
)

# 全局初始化三个分析器实例（类似 Spring 的 @Bean 单例注入）
# 应用启动时创建，所有请求共享这些实例
sales_analyzer = SalesAnalyzer()
user_analyzer = UserAnalyzer()
recommender = ProductRecommender()


# @app.get("/") 装饰器：注册 GET 请求路由，类似 Spring 的 @GetMapping("/")
@app.get("/")
def root():
    """根路径接口，返回 API 基本信息和可用接口列表"""
    return {
        "message": "商城数据分析 API 服务",
        "version": "1.0.0",
        "endpoints": {
            "/api/analytics/sales": "销售统计分析",
            "/api/analytics/users": "用户价值分析",
            "/api/analytics/recommend": "商品推荐"
        }
    }


# @app.get("/api/analytics/sales") 类似 @GetMapping("/api/analytics/sales")
# Query(default=7, ge=1, le=365)：定义查询参数，带默认值和范围验证
# - default=7：默认统计7天
# - ge=1：最小值为1（greater than or equal）
# - le=365：最大值为365（less than or equal）
@app.get("/api/analytics/sales")
def get_sales_stats(
    days: int = Query(default=7, ge=1, le=365, description="统计天数（1-365）")
):
    """
    获取销售统计分析接口
    - 返回销售汇总、Top10商品、分类统计、每日销售趋势
    """
    try:
        # 并行获取四类销售数据
        summary = sales_analyzer.get_sales_summary(days)        # 销售汇总
        top_products = sales_analyzer.get_top_products(10)      # Top10 热销商品
        category_stats = sales_analyzer.get_category_stats()    # 各分类统计
        daily_sales = sales_analyzer.get_daily_sales(days)      # 每日销售趋势

        # 统一返回格式：code + message + data
        return {
            "code": 200,
            "message": "success",
            "data": {
                "summary": summary,
                "topProducts": top_products,
                "categoryStats": category_stats,
                "dailySales": daily_sales
            }
        }
    except Exception as e:
        # 异常处理：返回500错误码和错误信息
        return {
            "code": 500,
            "message": f"获取销售统计失败: {str(e)}",
            "data": None
        }


@app.get("/api/analytics/users")
def get_user_analysis():
    """
    获取用户价值分析接口
    返回用户分类统计（高价值/普通/低活跃）和 Top20 高价值用户列表
    """
    try:
        user_type_stats = user_analyzer.get_user_type_stats()  # 各类型用户数量统计
        top_users = user_analyzer.get_top_users(20)            # Top20 高价值用户

        return {
            "code": 200,
            "message": "success",
            "data": {
                "userTypeStats": user_type_stats,
                "topUsers": top_users
            }
        }
    except Exception as e:
        return {
            "code": 500,
            "message": f"获取用户分析失败: {str(e)}",
            "data": None
        }


# 商品推荐接口：支持按分类筛选或全局热门
# category_id 为可选参数，不传则返回全局热门商品
@app.get("/api/analytics/recommend")
def get_recommendations(
    category_id: int = Query(default=None, description="分类 ID（可选）"),
    limit: int = Query(default=20, ge=1, le=100, description="返回数量（1-100）")
):
    """
    获取商品推荐接口
    - category_id: 分类 ID（可选，不传则返回全局热门商品）
    - limit: 返回数量，默认 20
    """
    try:
        hot_products = recommender.get_hot_products(category_id, limit)

        return {
            "code": 200,
            "message": "success",
            "data": {
                "hotProducts": hot_products
            }
        }
    except Exception as e:
        return {
            "code": 500,
            "message": f"获取商品推荐失败: {str(e)}",
            "data": None
        }


# 路径参数示例：/api/analytics/recommend/user/123
# {user_id} 是路径参数，类似 Spring 的 @PathVariable
@app.get("/api/analytics/recommend/user/{user_id}")
def get_user_recommendations(
    user_id: int,  # 路径参数，FastAPI 自动转换为 int 类型
    limit: int = Query(default=10, ge=1, le=50, description="返回数量（1-50）")
):
    """
    获取指定用户的个性化商品推荐
    - user_id: 用户 ID（路径参数）
    - limit: 返回数量，默认 10
    """
    try:
        recommendations = recommender.get_recommendations_for_user(user_id, limit)

        return {
            "code": 200,
            "message": "success",
            "data": {
                "recommendations": recommendations
            }
        }
    except Exception as e:
        return {
            "code": 500,
            "message": f"获取用户推荐失败: {str(e)}",
            "data": None
        }


# 健康检查接口：用于监控服务是否正常运行（类似 Spring Boot Actuator 的 /health）
@app.get("/api/health")
def health_check():
    """健康检查接口，返回服务运行状态"""
    return {
        "code": 200,
        "message": "服务运行正常",
        "data": None
    }


# __name__ == "__main__" 表示当前文件是作为主程序直接运行的（而非被其他模块 import）
# 这样写的好处是：当 app.py 被其他文件 import 时，下面的启动代码不会执行
if __name__ == "__main__":
    import uvicorn  # uvicorn 是一个高性能的 ASGI 服务器，类似于 Java 的 Tomcat
    # 启动 Web 服务器，host="0.0.0.0" 表示监听所有网卡（允许外部访问），port=8000 是端口号
    uvicorn.run(app, host="0.0.0.0", port=8000)
