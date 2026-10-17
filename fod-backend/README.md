# Fight Or Die · 服务端（fod-backend）

《Fight Or Die》文字 RPG 的服务端实现。提供账号认证、战斗流程托管与战绩统计等接口。

## 技术栈

| 类别 | 选型 |
|---|---|
| 语言 / 框架 | Java 23 · Spring Boot 4.1 |
| Web | Spring Web MVC |
| 认证 | Spring Security + JWT（无状态） |
| 持久层 | MyBatis 3（XML Mapper）· MySQL 8 |
| 缓存 | Caffeine |
| 接口文档 | springdoc-openapi（Swagger UI） |
| 容器化 | Docker · Docker Compose |

## 快速开始

### 方式一：Docker Compose

```bash
docker-compose up -d
```

### 方式二：本地运行

```bash
# 1) 初始化数据库表结构
mysql -u root -p < init.sql

# 2) 配置敏感项（复制模板后填入真实值，文件已被 .gitignore 排除）
cp src/main/resources/application-example.properties \
   src/main/resources/application-local.properties

# 3) 启动
./mvnw spring-boot:run
```

敏感配置项：`spring.datasource.username` / `spring.datasource.password` / `jwt.secret`，
亦可改为环境变量 `DB_USERNAME` / `DB_PASSWORD` / `JWT_SECRET`。

启动后打开 Swagger UI 查看全部接口与调用示例。

## 项目结构

```
src/main/java/com/fod/
├── fod_backend/
│   ├── controller/     接口层（Auth / Battle / Stats）
│   ├── service/        业务逻辑与会话管理
│   ├── security/       JWT 过滤器、令牌工具、安全配置
│   ├── config/         缓存 / MyBatis / 全局异常处理
│   └── dto/            请求与响应模型
├── entity/             领域实体（角色 / 怪物 / 技能 / 装备 等）
├── mapper/             MyBatis Mapper
└── system/
    ├── combat/         战斗系统（策略模式：普通攻击 / 防御 / 治疗 / AI 行动）
    ├── event/          事件总线
    └── factory/        实体工厂
```

## 设计要点

- **战斗系统复用引擎设计**：核心战斗采用策略模式 + 事件总线，与服务端之外的引擎保持一致的规则语义。
- **无状态认证**：JWT + Spring Security，适配多端访问，便于横向扩展。
- **缓存**：Caffeine 本地缓存承载排行榜与统计类高频只读查询。
- **接口文档**：Swagger 自动生成，先设计接口、再联调，降低协作成本。
- **环境可复现**：Docker Compose 一键拉起 MySQL + 应用容器。

## 开发状态

进行中。

## 相关仓库

本仓库为前后端分离全栈项目，前端位于仓库根目录的 `fod-frontend/`，总览见根目录 README。

- 重构前的控制台版本：https://github.com/Jiggale/FightOrDie
