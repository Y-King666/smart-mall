# mall-common — 公共模块

## 概述

后端各业务模块共用的基础模块：**实体、Mapper、安全与 ORM 配置、通用工具**。
本身不是可执行模块，只作为依赖被 `mall-admin` 和 `mall-ai` 引用。

> 因为 Mapper 都放在这里，启动类扫描的是本模块的包：
> `@MapperScan("com.yking.mallcommon.mapper")`。

## 包结构

```
com.yking.mallcommon
├── Result                       # 统一响应体 Result<T>（code / message / data）
├── entity/                      # 7 张表对应的实体（与表名一一对应）
│   ├── SysUser                  # sys_user（password 设为不参与查询）
│   ├── PmsCategory              # pms_category（children 为非表字段，用于组装分类树）
│   ├── PmsProduct               # pms_product
│   ├── OmsCartItem              # oms_cart_item
│   ├── OmsOrder                 # oms_order
│   ├── OmsOrderItem             # oms_order_item（封面图/简介为非表字段，查询时关联填充）
│   └── AiChatMessage            # ai_chat_message
├── mapper/                      # 7 个 Mapper 接口（MyBatis-Plus BaseMapper）
├── service/                     # 跨模块共用的业务逻辑（含 impl）
│   └── OrderCancelService       # 订单取消：用户端接口与 AI 客服共用同一套
│                                # 库存/销量回退规则（含 impl）
├── config/
│   ├── SecurityConfig           # 放行清单、无状态会话、密码编码器
│   ├── JwtAuthFilter            # 解析令牌 → 查库校验用户状态 → 写入 SecurityContext
│   ├── MybatisPlusConfig        # 分页插件
│   ├── MybatisPlusMetaObjectHandler  # create_time / update_time 自动填充
│   └── WebMvcConfig             # /api/uploads/** 静态资源映射
├── security/TokenBlacklist      # 退出登录后的令牌吊销集合（内存）
├── exception/
│   ├── BusinessException        # 业务异常（对外返回 code + message）
│   └── GlobalExceptionHandler   # 全局异常处理：业务异常 / 参数校验 / 403 / 404 / 兜底 500
└── util/
    ├── JwtUtil                  # 生成与解析 JWT
    ├── AuthUtil                 # 取当前用户 ID（SecurityContext）与令牌（Authorization 头）
    └── DateTimeUtil             # 时间统一格式化为 yyyy-MM-dd HH:mm:ss
```

## 约定

- 实体使用 Lombok `@Data`，表名用 `@TableName` 显式声明。
- 逻辑删除字段 `deleted`（`sys_user`/`pms_category`/`pms_product`/`oms_order` 四张表有）：
  `PmsCategory`/`PmsProduct`/`OmsOrder` 三个实体显式标注 `@TableLogic`，`SysUser` 靠全局配置接管，
  查询都会自动带上 `deleted = 0`。
- 时间字段统一用 `LocalDateTime`（`SysUser` 的历史字段是 `java.util.Date`，属遗留）。
- 非表字段（关联查询填充用的）用 `@TableField(exist = false)` 标注。
- 所有 Controller 返回 `Result<T>`（SSE 流式接口返回 `SseEmitter`，是唯一例外），
  异常交给 `GlobalExceptionHandler` 统一处理。
