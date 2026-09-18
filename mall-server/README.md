# mall-server — 后端服务

## 概述

Maven 多模块后端项目，基于 **Spring Boot 4.1.1**，包含 3 个子模块。

## 模块结构

```
mall-server/
├── pom.xml              # 父 POM：聚合子模块 + 统一版本管理
├── mall-common/         # 公共模块（实体/工具/安全/ORM）
├── mall-ai/             # AI 客服模块（Spring AI 2.0.1）
└── mall-admin/          # 统一后端服务（可执行 JAR）
```

## 依赖关系

```
mall-admin（可执行 JAR，启动入口）
 ├── mall-common（公共基础）
 └── mall-ai（AI 客服）
      └── mall-common
```

## 版本管理

所有依赖版本在父 `pom.xml` 的 `<properties>` 和 `<dependencyManagement>` 中集中管理：

| 依赖 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.1.1 | 基础框架 |
| Java | 17 | 最低要求 |
| MyBatis-Plus | 3.5.16 | ORM（mybatis-plus-spring-boot4-starter） |
| Spring AI BOM | 2.0.1 | AI 框架 |
| JJWT | 0.12.6 | JWT 令牌 |
| Lombok | （Boot 管理） | 代码简化 |

## 启动方式

```bash
# 编译所有模块
mvn clean compile

# 运行（mall-admin 是启动入口）
# -am 表示同时构建它依赖的 mall-common、mall-ai；不加的话，
# 本地仓库里还没有这两个模块的 SNAPSHOT 时会解析失败
mvn -pl mall-admin -am spring-boot:run

# 或在 IDE 中运行
# 主类：com.yking.malladmin.MallAdminApplication（位于 mall-admin 模块）
```

## 配置文件

`mall-admin/src/main/resources/application.yml` 是唯一的配置文件，包含：
- 服务端口（8080）
- 数据库连接
- Spring AI 配置（通义千问，API Key 从环境变量 `AI_API_KEY` 读取）
- JWT 密钥和过期时间
- MyBatis-Plus 配置
- 文件上传目录（`file.upload-dir: ./uploads`，相对路径基于工作目录，由 `mall-admin/pom.xml` 中
  spring-boot-maven-plugin 的 `workingDirectory` 固定到项目根，避免不同启动方式生成两个 uploads）

## 包扫描

启动类配置：
```java
@SpringBootApplication(scanBasePackages = "com.yking")
@MapperScan("com.yking.mallcommon.mapper")
```

Mapper 接口统一放在 **mall-common** 模块（`com.yking.mallcommon.mapper`），
三个模块共用同一批 Mapper，因此扫描的是 common 的包。

## 开发规范

- 所有依赖加中文注释说明用途
- 实体类使用 Lombok `@Data` 注解
- Controller 返回统一 `Result<T>` 格式
- 异常通过 `GlobalExceptionHandler` 统一处理
- 使用 `@Valid` 进行请求参数校验
