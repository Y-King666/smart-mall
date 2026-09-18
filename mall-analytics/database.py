"""
数据库连接配置
使用 SQLAlchemy 连接 MySQL 数据库
"""
# 从 sqlalchemy 导入 create_engine 函数，用于创建数据库连接引擎
# sqlalchemy 是 Python 最流行的 ORM 框架，类似于 Java 的 MyBatis/Hibernate
from sqlalchemy import create_engine
import os
# dotenv 用于读取 .env 文件中的环境变量，避免把密码等敏感信息硬编码在代码中
from dotenv import load_dotenv

# 加载项目根目录下的 .env 文件，将其中的配置注入到系统环境变量中
load_dotenv()

# 从环境变量读取数据库配置，如果环境变量未设置则使用默认值
# 这类似于 Spring Boot 的 application.yml 配置方式
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = int(os.getenv("DB_PORT", "3306"))
DB_USER = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "root")
DB_NAME = os.getenv("DB_NAME", "mall_db")

# 拼接数据库连接 URL，格式为：mysql+pymysql://用户名:密码@主机:端口/数据库名
# mysql+pymysql 表示使用 PyMySQL 驱动连接 MySQL（类似于 JDBC 的 jdbc:mysql://）
# charset=utf8mb4 支持中文及 emoji 等特殊字符
DATABASE_URL = f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}?charset=utf8mb4"

# 创建数据库连接引擎（类似 Java 的 DataSource）
# pool_pre_ping=True：每次从连接池取连接前先检测是否可用，避免拿到已断开的连接
# pool_recycle=3600：连接超过1小时自动回收重建，防止长时间空闲被 MySQL 服务端主动断开
# echo=False：是否打印执行的 SQL 语句，开发调试时可设为 True
engine = create_engine(
    DATABASE_URL,
    pool_pre_ping=True,      # 连接前检查是否有效
    pool_recycle=3600,       # 1小时回收连接
    echo=False               # 不打印 SQL 日志（生产环境设为 False）
)

def get_db_connection():
    """
    获取数据库连接引擎
    :return: SQLAlchemy Engine 对象，可供 pandas.read_sql 等方法使用
    """
    return engine

def test_connection():
    """
    测试数据库连接是否正常
    :return: 连接成功返回 True，失败返回 False
    """
    try:
        # with 语句会自动管理连接的打开和关闭，类似 Java 的 try-with-resources
        with engine.connect() as conn:
            print("✅ 数据库连接成功")
            return True
    except Exception as e:
        print(f"❌ 数据库连接失败: {e}")
        return False

# 当直接运行此文件时执行测试连接（python database.py）
# 如果被其他文件 import 则不会执行，避免副作用
if __name__ == "__main__":
    test_connection()
