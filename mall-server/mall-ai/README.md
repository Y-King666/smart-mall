# mall-ai — AI 客服模块

## 概述

基于 **Spring AI 2.0.1** 的智能客服模块，通过 OpenAI 兼容协议接入通义千问
（模型 `qwen3.7-flash`，嵌入模型 `text-embedding-v4`）。
本模块不是可执行模块，被 `mall-admin` 依赖后随统一后端一起启动。

## 包结构

```
com.yking.mallai
├── controller/
│   └── AiChatController     # 正式客服接口：对话（SSE）+ 会话历史
├── service/                 # AiChatService（含 impl）：对话、历史、会话管理
├── function/                # Function Calling 工具（四个查询 + 一个操作）
│   ├── ProductFunction      # getProductInfo 商品模糊查询、getHotProducts 热销榜
│   ├── OrderFunction        # getMyOrders 当前用户的历史订单、cancelMyOrder 按订单号取消订单
│   └── CartFunction         # getMyCart 当前用户的购物车
├── dto/                     # AiChatRequest / AiMessageVO / AiSessionVO
└── config/RagConfig         # 构建向量库并提供检索增强 advisor 给正式客服使用
                             # （嵌入模型是把 application.yml 自动装配的 EmbeddingModel 注入进来用）
```

## 正式客服接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/portal/ai/chat` | 发起对话，**SSE 流式**返回 |
| GET | `/api/portal/ai/sessions` | 我的会话列表（按最近对话时间倒序） |
| GET | `/api/portal/ai/sessions/{sessionId}/messages` | 某个会话的历史消息 |
| DELETE | `/api/portal/ai/sessions/{sessionId}` | 删除某个会话 |
| DELETE | `/api/portal/ai/sessions` | 清空我的全部会话 |

需要登录：会话按用户隔离，所有查询与删除都以当前登录用户为作用域。

### SSE 事件格式

对话接口逐段推送 JSON，前端按空行切分事件：

```
data: {"type":"content","content":"您好"}

data: {"type":"content","content":"，有什么可以帮您？"}

data: {"type":"done","sessionId":"xxxx"}
```

不套 `Result` 统一响应体——流式输出无法用一次性包装的结构承载。

### 对话流程

1. 先把用户提问落库（`ai_chat_message`）；
2. 取该会话最近 20 条消息作为上下文；
3. 组装带系统提示词、查询工具与知识库检索的 `ChatClient` 发起流式请求；
4. 每收到一段内容就推 `content` 事件，同时累加；
5. 结束时把完整回复落库，再推 `done` 事件。

模型异常时会把错误转成一句兜底文案，同样推给前端并落库，保证「会话记录」与用户所见一致。

### 知识库检索（RAG）

每次对话都会先用用户提问检索一次 PDF 知识库，把相似度达标的片段拼进 prompt：

- 知识库来自 `mall-ai/src/main/resources/Consultation2025.pdf`，应用启动时由 `RagConfig` 读取、切分并向量化；
- 检索参数在 `RagConfig` 中：`topK = 4`、相似度阈值 `0.5`，低于阈值的片段不注入，避免无关内容干扰回答；
- 向量库虽是内存版（`SimpleVectorStore`），但会**持久化到文件**（路径见 `rag.vector-store-path`，默认 `./data/vector-store.json`）：
  向量连同文本一起落盘，之后启动直接 `load`（纯反序列化，不调用嵌入模型），只在
  **首次运行**或**PDF 内容变化**时才重新向量化，避免反复消耗嵌入模型额度；
- 判断是否可复用靠 PDF 的 SHA-256 指纹（同名 `.sha256` 旁文件）。指纹不一致或文件损坏都会自动重建；
- 知识库初始化失败**不会阻止应用启动**，但会打 WARN 并明确提示「RAG 未生效」——此时问答退化为普通对话。

## Function Calling 工具

前四个让模型能查到真实数据，第五个能替用户操作数据，都避免编造：

- **商品**：按名称关键字模糊查询（只返回上架商品，最多 5 条）、按销量取热销榜；
- **订单查询**：当前登录用户的历史订单（订单号、下单时间、金额、支付状态、商品明细）；
- **订单取消**：按订单号取消当前用户自己的一笔订单；
- **购物车**：当前登录用户的购物车（商品、数量、是否选中、已选中总金额）。

订单与购物车工具**按请求新建实例**，把 `userId` 绑在实例上，而不是在工具里读登录态：
工具是在模型回调时才执行的，那时可能跑在 Reactor 的弹性线程上，ThreadLocal 里的登录信息不可靠。
同时 userId 从构造参数传入，也保证模型只能查到当前用户自己的数据。

### 取消订单（写操作）

这是一组工具里唯一会改动数据的，因此有三道约束：

1. **提示词层面**：用户说要取消但没给订单号时，先问清楚是哪一笔（可调 `getMyOrders` 把订单列出来
   供他参考），**不许猜、不许编造订单号**；只有拿到订单号后才调用工具。
2. **工具层面**：`cancelMyOrder` 只按「订单号 + 当前 userId」查订单，查不到就把用户给的那串
   订单号回显出来、提示他确认——所以用户报别人的订单号也取消不了。
3. **业务层面**：取消动作调用的是公共模块的 `OrderCancelService`，与用户端「我的订单」里
   点取消**完全同一套规则**（校验归属、恢复库存、已支付订单扣回销量），不会出现两套逻辑算错账。

工具失败时（`BusinessException`，如订单已取消）不抛异常，而是返回一句说明文字，让模型能把
真实原因转述给用户；其它运行期异常仍会抛出，由对话服务兜底成一条「AI 服务暂时不可用」的文案。

## 配置

配置在后端的 `mall-admin/src/main/resources/application.yml` 的 `spring.ai` 下：

- 通义千问 API Key 从**环境变量 `AI_API_KEY`** 读取（未配置时用占位符，AI 对话会失败）；
- 对话模型 `qwen3.7-flash`，temperature 0.5；嵌入模型 `text-embedding-v4`。
