"""
数据分析核心模块
包含：销售分析、用户分析、商品推荐
"""
# pandas 是 Python 最强大的数据分析库，类似于 Java 的 Apache Spark DataFrame
# read_sql 可以直接执行 SQL 并将结果转为 DataFrame（类似 MyBatis 的查询结果映射）
import pandas as pd
# 从 database 模块导入数据库连接函数
from database import get_db_connection
# datetime 用于日期处理，timedelta 用于日期加减运算
from datetime import datetime, timedelta


def category_subtree_ids(engine, category_id):
    """
    取某个分类及其全部后代的 id

    分类表以 parent_id 自关联。与后台商品管理的筛选口径保持一致：
    选父分类 = 查它下面所有子分类的商品（商品都挂在叶子分类上，
    只按 id 精确匹配的话，选「数字课程」这种父分类会一件都查不出来）。
    """
    df = pd.read_sql("SELECT id, parent_id FROM pms_category WHERE deleted = 0", engine)
    children = {}
    for row in df.to_dict('records'):
        children.setdefault(row['parent_id'], []).append(row['id'])

    ids, pending = [], [category_id]
    while pending:
        cid = pending.pop()
        ids.append(cid)
        pending.extend(children.get(cid, []))
    return ids



class SalesAnalyzer:
    """销售数据分析类，提供商品销量、分类统计、每日销售等分析功能"""

    def __init__(self):
        # 初始化时获取数据库连接引擎，后续所有查询都复用这个连接
        self.engine = get_db_connection()

    def get_top_products(self, limit=10):
        """
        获取销量 Top N 商品
        :param limit: 返回的商品数量，默认10
        :return: 商品列表，每项包含 id、name、price、total_sales、total_amount
        """
        # 销量一律取 pms_product.sales_count —— 用户端展示的就是这个字段，
        # 从订单明细累加会与用户端对不上（商品还带有初始化销量，没有对应订单）。
        # 销售额按「累计销量 × 现价」估算，与销量自洽；
        # 只从订单算的话，初始化销量那一部分金额会全部为 0，表格看起来是坏的。
        # %s 是 PyMySQL 的参数占位符，类似 JDBC 的 ?
        sql = """
        SELECT p.id, p.name, p.price,
               p.sales_count as total_sales,
               p.sales_count * p.price as total_amount
        FROM pms_product p
        WHERE p.deleted = 0
        ORDER BY total_sales DESC
        LIMIT %s
        """
        try:
            # pd.read_sql：执行 SQL 查询，结果自动转为 DataFrame 表格结构
            # params=(limit,)：元组形式传递参数，注意逗号不能省略
            df = pd.read_sql(sql, self.engine, params=(limit,))
            # to_dict('records')：将 DataFrame 转为字典列表，类似 Java 的 List<Map<String, Object>>
            return df.to_dict('records')
        except Exception as e:
            print(f"获取热销商品失败: {e}")
            return []

    def get_category_stats(self):
        """获取各分类销售统计"""
        # 与商品维度同一口径：销量取商品累计销量，销售额按「累计销量 × 现价」估算。
        # 两个指标都用子查询算，避免与商品表 JOIN 后行数膨胀、把销量重复累加。
        sql = """
        SELECT c.id, c.name as category_name,
               COUNT(DISTINCT p.id) as product_count,
               COALESCE((
                   SELECT SUM(p2.sales_count) FROM pms_product p2
                   WHERE p2.category_id = c.id AND p2.deleted = 0
               ), 0) as total_sales,
               COALESCE((
                   SELECT SUM(p2.sales_count * p2.price) FROM pms_product p2
                   WHERE p2.category_id = c.id AND p2.deleted = 0
               ), 0) as total_amount
        FROM pms_category c
        LEFT JOIN pms_product p ON c.id = p.category_id AND p.deleted = 0
        WHERE c.deleted = 0
        GROUP BY c.id, c.name
        ORDER BY total_sales DESC
        """
        try:
            df = pd.read_sql(sql, self.engine)
            return df.to_dict('records')
        except Exception as e:
            print(f"获取分类统计失败: {e}")
            return []

    def get_daily_sales(self, days=7):
        """
        获取近 N 天的每日销售统计
        :param days: 统计天数，默认7天
        :return: 每日销售数据列表
        """
        # 计算时间范围：从今天往前推 days 天
        end_date = datetime.now()
        start_date = end_date - timedelta(days=days)

        # 只统计已支付订单：取消的订单会把销售额与订单数一起回退
        sql = """
        SELECT DATE(o.create_time) as sale_date,
               COUNT(DISTINCT o.id) as order_count,
               COALESCE(SUM(o.total_amount), 0) as total_sales,
               COUNT(DISTINCT o.user_id) as user_count
        FROM oms_order o
        WHERE o.create_time >= %s AND o.pay_status = 1 AND o.deleted = 0
        GROUP BY DATE(o.create_time)
        ORDER BY sale_date
        """
        try:
            df = pd.read_sql(sql, self.engine, params=(start_date,))
            return df.to_dict('records')
        except Exception as e:
            print(f"获取每日销售失败: {e}")
            return []

    def get_sales_summary(self, days=7):
        """
        获取销售汇总数据（订单总数、用户总数、销售总额、平均订单金额）
        :param days: 统计天数，默认7天
        :return: 汇总数据字典
        """
        end_date = datetime.now()
        start_date = end_date - timedelta(days=days)

        # 只统计已支付订单，与仪表盘、订单统计口径一致
        sql = """
        SELECT
            COUNT(DISTINCT o.id) as total_orders,
            COUNT(DISTINCT o.user_id) as total_users,
            COALESCE(SUM(o.total_amount), 0) as total_sales,
            COALESCE(AVG(o.total_amount), 0) as avg_order_amount
        FROM oms_order o
        WHERE o.create_time >= %s AND o.pay_status = 1 AND o.deleted = 0
        """
        try:
            df = pd.read_sql(sql, self.engine, params=(start_date,))
            # 查询结果只有一行汇总数据，取第一条记录
            result = df.to_dict('records')[0] if len(df) > 0 else {}
            # MySQL 的 SUM/AVG 返回 Decimal 类型，JSON 序列化不支持，需要转为 float
            for key in ['total_sales', 'avg_order_amount']:
                # hasattr 检查：确保字段存在且是 Decimal 类型才转换
                if key in result and hasattr(result[key], '__float__'):
                    result[key] = float(result[key])
            return result
        except Exception as e:
            print(f"获取销售汇总失败: {e}")
            return {}


class UserAnalyzer:
    """用户价值分析类，基于用户消费行为进行分层（简化版 RFM 模型）"""

    def __init__(self):
        # 初始化数据库连接引擎
        self.engine = get_db_connection()

    def analyze_user_value(self):
        """
        用户价值分析（简化版 RFM 模型）
        RFM = Recency(最近消费) + Frequency(消费频次) + Monetary(消费金额)
        :return: 用户列表，包含订单数、消费总额、最近/首次消费时间、用户分类
        """
        # 查询所有普通用户的消费统计（排除管理员等角色）
        # MAX/MIN 获取最近和首次消费时间，用于 RFM 分析
        # 只关联已支付订单：消费频次与金额都以成交为准，取消的订单不算消费
        # （条件写在 ON 里，没成交过的用户仍会出现在列表中，只是金额为 0）
        sql = """
        SELECT u.id, u.username, u.nickname,
               COUNT(o.id) as order_count,
               COALESCE(SUM(o.total_amount), 0) as total_spent,
               MAX(o.create_time) as last_order_time,
               MIN(o.create_time) as first_order_time
        FROM sys_user u
        LEFT JOIN oms_order o ON u.id = o.user_id AND o.pay_status = 1 AND o.deleted = 0
        WHERE u.role = 'USER'
        GROUP BY u.id, u.username, u.nickname
        ORDER BY total_spent DESC
        """
        try:
            df = pd.read_sql(sql, self.engine)

            # 定义用户分类规则（基于消费频次和金额）
            # axis=1 表示按行处理，每行调用一次 classify_user 函数
            def classify_user(row):
                if row['order_count'] > 3 and float(row['total_spent']) > 500:
                    return '高价值用户'  # 高频高额
                elif row['order_count'] > 1:
                    return '普通用户'     # 有一定消费
                else:
                    return '低活跃用户'   # 几乎不消费

            # 将分类结果作为新列添加到 DataFrame
            df['user_type'] = df.apply(classify_user, axis=1)

            # 将 total_spent 列从 Decimal 转为 float，便于后续 JSON 序列化
            df['total_spent'] = df['total_spent'].astype(float)

            return df.to_dict('records')
        except Exception as e:
            print(f"用户价值分析失败: {e}")
            return []

    def get_user_type_stats(self):
        """
        获取用户分类统计（各类型用户数量）
        :return: 字典，如 {'高价值用户': 5, '普通用户': 10, '低活跃用户': 20}
        """
        users = self.analyze_user_value()
        if not users:
            return {}

        # 初始化计数器，确保三种类型都有初始值 0
        stats = {
            '高价值用户': 0,
            '普通用户': 0,
            '低活跃用户': 0
        }

        # 遍历用户列表，按类型累加计数
        for user in users:
            user_type = user.get('user_type', '低活跃用户')  # 默认为低活跃
            stats[user_type] = stats.get(user_type, 0) + 1

        return stats

    def get_top_users(self, limit=20):
        """
        获取 Top N 高价值用户（按消费总额排序）
        :param limit: 返回用户数量，默认20
        :return: 用户列表
        """
        users = self.analyze_user_value()
        # Python 列表切片：users[:limit] 取前 limit 个元素，类似 Java 的 subList(0, limit)
        return users[:limit]


class ProductRecommender:
    """商品推荐类，提供热门商品查询和个性化推荐功能"""

    def __init__(self):
        # 初始化数据库连接引擎
        self.engine = get_db_connection()

    def get_hot_products(self, category_id=None, limit=20):
        """
        获取热门商品（基于销量排序）
        :param category_id: 分类ID，为 None 时查询所有分类
        :param limit: 返回商品数量，默认20
        :return: 商品列表
        """
        if category_id:
            # 按分类查询：含子分类。商品挂在叶子分类上，父分类只是分组，
            # 只按 id 精确匹配的话，选「数字课程」这种父分类会一件都查不出来。
            subtree_ids = category_subtree_ids(self.engine, category_id)
            placeholders = ', '.join(['%s'] * len(subtree_ids))
            sql = f"""
            SELECT p.id, p.name, p.price, p.cover_image as image, p.category_id,
                   c.name as category_name,
                   p.sales_count as total_sales,
                   p.sales_count * p.price as total_amount
            FROM pms_product p
            LEFT JOIN pms_category c ON p.category_id = c.id
            WHERE p.category_id IN ({placeholders}) AND p.deleted = 0
            ORDER BY total_sales DESC
            LIMIT %s
            """
            params = (*subtree_ids, limit)  # 元组形式传递：子树 id + limit
        else:
            # 查询所有分类的热门商品
            sql = """
            SELECT p.id, p.name, p.price, p.cover_image as image, p.category_id,
                   c.name as category_name,
                   p.sales_count as total_sales,
                   p.sales_count * p.price as total_amount
            FROM pms_product p
            LEFT JOIN pms_category c ON p.category_id = c.id
            WHERE p.deleted = 0
            ORDER BY total_sales DESC
            LIMIT %s
            """
            params = (limit,)  # 注意：单元素元组必须加逗号

        try:
            df = pd.read_sql(sql, self.engine, params=params)
            return df.to_dict('records')
        except Exception as e:
            print(f"获取热门商品失败: {e}")
            return []

    def get_recommendations_for_user(self, user_id, limit=10):
        """
        为用户推荐商品（简单版：推荐用户未购买过的热门商品）
        :param user_id: 用户ID
        :param limit: 推荐商品数量，默认10
        :return: 推荐商品列表
        """
        # 推荐逻辑：查询所有热门商品，排除用户已购买过的
        # NOT IN 子查询：找出该用户已经购买过的商品ID（只算已支付订单——
        # 待支付/已取消的订单不算买过，否则刚取消的商品就再也不会被推荐）
        sql = """
        SELECT p.id, p.name, p.price, p.cover_image as image, p.category_id,
               p.sales_count as total_sales
        FROM pms_product p
        WHERE p.deleted = 0 AND p.id NOT IN (
            SELECT DISTINCT oi.product_id
            FROM oms_order_item oi
            JOIN oms_order o ON oi.order_id = o.id
            WHERE o.user_id = %s AND o.pay_status = 1 AND o.deleted = 0
        )
        ORDER BY total_sales DESC
        LIMIT %s
        """
        try:
            df = pd.read_sql(sql, self.engine, params=(user_id, limit))
            return df.to_dict('records')
        except Exception as e:
            print(f"获取用户推荐失败: {e}")
            return []
