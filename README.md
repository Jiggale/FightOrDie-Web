# Fight Or Die · 全栈 RPG 平台

一个从零搭建的**前后端分离全栈项目**：以一款文字 RPG 游戏为载体，将纯控制台的 Java 游戏引擎重构为
**Spring Boot REST 服务 + Vue 3 单页应用 + MySQL 持久化**的完整 Web 系统。

```bash
# 启动后端（终端一）
cd fod-backend && ./mvnw spring-boot:run        # http://localhost:8080

# 启动前端（终端二）
cd fod-frontend && npm install && npm run dev   # http://localhost:5173
```

---

## 技术栈

| 层次 | 技术选型 |
|---|---|
| **前端** | Vue 3.4 · TypeScript · Vite 5 · Pinia · Vue Router 4 · Tailwind CSS 3 |
| **后端** | Java 23 · Spring Boot 4.1 · Spring Web MVC |
| **安全** | Spring Security · JWT（jjwt，无状态认证） |
| **持久层** | MyBatis 3（XML Mapper）· MySQL 8 · HikariCP |
| **缓存** | Spring Cache 抽象 · Caffeine |
| **工程化** | Maven · springdoc-openapi（Swagger UI）· Docker · Docker Compose |

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│  浏览器  http://localhost:5173                               │
│  ┌───────────────┐   ┌──────────────────────────┐            │
│  │  Vue 3 SPA    │──▶│  Vite Dev Server         │            │
│  │  HomeView     │   │  /api 反向代理 ──────────┼───┐        │
│  │  BattleView   │   │  静态资源托管            │   │        │
│  └───────────────┘   └──────────────────────────┘   │        │
└─────────────────────────────────────────────────────│────────┘
                                                      ▼
┌─────────────────────────────────────────────────────────────┐
│  Spring Boot  http://localhost:8080                         │
│  ┌───────────┐ ┌──────────┐ ┌─────────┐ ┌────────────────┐  │
│  │ Controller│▶│ Service  │▶│ MyBatis │▶│     MySQL      │  │
│  │  (REST)   │ │ 业务编排 │ │  Mapper │ │  表: battle_    │  │
│  └───────────┘ └──────────┘ └─────────┘ │     history,    │  │
│  ┌───────────────────────────────────┐  │     player_stats│  │
│  │ JwtAuthFilter  →  Spring Security │  └────────────────┘  │
│  │ GlobalExceptionHandler (@Advice)  │                       │
│  │ Caffeine Cache (@Cacheable)       │                       │
│  └───────────────────────────────────┘                       │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  游戏引擎（策略模式 + 事件总线，从原版完整迁移）        │  │
│  │  CombatSystem · 4×CombatStrategy · EventBus · Factory │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 核心功能

**战斗系统**（Web 化改造）
- 回合制战斗：攻击 / 防御 / 治疗 / 逃跑四种行动
- 战斗内核复用**策略模式**（`CombatStrategy` 接口 + 4 个具体策略），新增 AI 行动无需改动既有代码
- 怪物按 `AIType` 差异化决策，伤害计算含暴击与闪避判定
- 会话管理：`ConcurrentHashMap` 托管多场并发战斗，以 `sessionId` 隔离状态

**战绩与排行榜**（持久化改造）
- 每场战斗结算后写入 MySQL，`@Transactional` 保证战绩与统计两表数据一致
- 排行榜 / 玩家统计 / 战斗历史三类查询接口
- `@Cacheable` / `@CacheEvict` 注解式缓存，写操作后自动失效，兼顾性能与一致性

**认证授权**（原版完全不具备）
- 登录签发 JWT，服务端无会话状态，便于横向扩展
- `OncePerRequestFilter` 统一解析 `Authorization: Bearer` 头
- CORS 跨域配置，支持 Vite 开发服务器与前后端分离部署

**工程化能力**
- `@RestControllerAdvice` 全局异常处理，统一返回结构化 JSON，接口不再散落 try-catch
- Swagger UI 自动生成接口文档，支持在线调试
- `Dockerfile` + `docker-compose.yml` 一键拉起 MySQL + 应用
- JUnit 5 + `@SpringBootTest` 集成测试

## 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/login` | 登录并签发 JWT |
| POST | `/api/battle/start` | 创建战斗会话 |
| POST | `/api/battle/{id}/action` | 执行行动（1 攻击 / 2 防御 / 3 治疗） |
| POST | `/api/battle/{id}/flee` | 逃跑并结算 |
| GET | `/api/battle/{id}/state` | 查询战斗状态 |
| GET | `/api/strategies` | 可选策略列表 |
| GET | `/api/stats/player/{name}` | 玩家统计 |
| GET | `/api/stats/leaderboard` | 金币排行榜 |
| GET | `/api/stats/history/{name}` | 战斗历史 |

启动后访问 `http://localhost:8080/swagger-ui.html` 可在线调试全部接口。

## 本地运行

### 1. 数据库

```bash
mysql -u root -p < fod-backend/init.sql
```

### 2. 后端配置

敏感配置不提交仓库，复制模板后填入真实值：

```bash
cp fod-backend/src/main/resources/application-example.properties \
   fod-backend/src/main/resources/application-local.properties
# 编辑 application-local.properties，填入数据库账号密码与 JWT 密钥
```

或改用环境变量：`DB_USERNAME` / `DB_PASSWORD` / `JWT_SECRET`。

### 3. 启动

```bash
cd fod-backend && ./mvnw spring-boot:run    # 后端 :8080
cd fod-frontend && npm install && npm run dev   # 前端 :5173
```

前端 `vite.config.ts` 已将 `/api` 代理至 8080，无需处理跨域。

## 项目结构

```
.
├── fod-backend/                 Spring Boot 服务端
│   ├── src/main/java/com/fod/
│   │   ├── fod_backend/
│   │   │   ├── controller/      REST 接口层
│   │   │   ├── service/         业务逻辑与会话管理
│   │   │   ├── security/        JWT 过滤器 / 安全配置
│   │   │   ├── config/          缓存 / MyBatis / 全局异常
│   │   │   └── dto/             请求响应模型
│   │   ├── entity/              领域实体
│   │   ├── mapper/              MyBatis Mapper 与 XML
│   │   └── system/
│   │       ├── combat/          战斗系统（策略模式）
│   │       ├── event/           事件总线（优先级 / 背压 / 去重）
│   │       └── factory/         实体工厂
│   ├── init.sql                 建表脚本
│   ├── Dockerfile
│   └── docker-compose.yml
│
└── fod-frontend/                Vue 3 客户端
    ├── src/
    │   ├── api/                 axios 实例（JWT 拦截器）+ 接口封装
    │   ├── store/               Pinia 状态管理
    │   ├── router/              路由配置
    │   └── views/               HomeView（排行榜）· BattleView（战斗）
    └── vite.config.ts           开发服务器与代理配置
```

## 设计要点

| 设计 | 说明 |
|---|---|
| **策略模式驱动战斗** | `CombatStrategy` 抽象 + 4 个实现，符合开闭原则 |
| **事件总线解耦** | 战斗事件异步派发，避免战斗逻辑与统计、日志直接耦合 |
| **Vite 代理消除跨域** | 开发期 `/api` 转发至后端，前端无感同源；后端 CORS 作双保险 |
| **JWT 自动登录闭环** | 前端页面加载静默换取 Token，axios 拦截器统一注入，用户零感知 |
| **注解式缓存** | `@CacheEvict` 在写操作后失效缓存，避免手工维护一致性问题 |
| **环境配置外置** | 密钥全部走环境变量，仓库内不含任何真实凭据 |

## 相关仓库

- 重构前的控制台版本：https://github.com/Jiggale/FightOrDie
