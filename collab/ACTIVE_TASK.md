# 当前任务

## TASK-002：GitHub Actions CI

### 状态

**已批准，等待 CodeBuddy 实施。**

### 目标

为 `main` 的推送和 Pull Request 增加自动验证，使以下两步在 GitHub 上稳定运行：

1. 后端完整测试；
2. 前端生产构建。

### 分支

`task-002-ci`

### 实施要求

1. 新建 `.github/workflows/ci.yml`。
2. 工作流在 `push` 到 `main` 和针对 `main` 的 `pull_request` 时运行。
3. 后端任务使用 Ubuntu、Java 23 和 MySQL 8 服务容器。
4. 后端通过 `fod-backend/init.sql` 初始化测试数据库。
5. 后端执行 `fod-backend/mvnw test`，`mvnw` 在 CI 中必须先补执行权限。
6. 前端使用 Node.js 22，执行 `npm ci` 和 `npm run build`。
7. 开启 Maven 与 npm 缓存，减少后续执行时间。
8. 设置并发取消：同一分支出现新提交时，取消旧的 CI 运行。
9. CI 必须使用测试专用环境变量，不得提交真实密码或 JWT 密钥。

### 明确不做

- 不修改业务代码、接口、数值或页面。
- 不部署服务器。
- 不制作 Docker 镜像发布流程。
- 不添加 E2E、视觉回归或 Playwright。
- 不把数据库密码写入仓库文件。

### 验收标准

- [ ] PR 页面出现后端测试和前端构建检查。
- [ ] 后端任务启动 MySQL 8 后完成数据库初始化。
- [ ] 后端 32 项测试全部通过。
- [ ] 前端 `npm ci` 和 `npm run build` 通过。
- [ ] 失败日志能明确指出是后端、数据库还是前端构建失败。
- [ ] 不包含真实密钥。
- [ ] `collab/HANDOFF.md` 记录运行结果和 PR 链接。

### 交付

CodeBuddy 在 `task-002-ci` 分支完成后推送并创建 PR，不直接合并 `main`。
