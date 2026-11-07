# 实施交接

> 此文件由实施方在每次任务完成后填写。没有实现或没有测试证据时，不得标记为完成。
>
> TASK-001 的详细交接内容保留在 git 历史中（合并前提交 `bb93237`、`fbb150e`），
> 任务索引见 `collab/TASK_ARCHIVE.md`，审查结论见 `collab/REVIEW_LOG.md`。

## 当前状态

TASK-002（GitHub Actions CI）已实现，本地可验证项全部通过，
并已在 PR #2 上完成首次真实运行：**All checks have passed · 2 successful checks**，无冲突。

## 任务编号

TASK-002

## 实现目标

为 `main` 的推送与 Pull Request 增加自动验证：后端完整测试与前端生产构建。

## 改动文件

- `.github/workflows/ci.yml`（新建）
- `collab/HANDOFF.md`

未改动任何业务代码、接口、数值或页面。

## 实现说明

### 触发与并发

- `push` 到 `main`、`pull_request` 目标为 `main` 时运行。
- `concurrency: ci-${{ github.workflow }}-${{ github.ref }}` + `cancel-in-progress: true`，
  同一分支新提交会取消旧的运行。
- `permissions: contents: read`，仅需读权限。

### backend 任务

| 项 | 内容 |
|---|---|
| 运行环境 | `ubuntu-latest`，`timeout-minutes: 20` |
| JDK | `actions/setup-java@v4`，temurin，版本 `23`，`cache: maven`（依赖路径 `fod-backend/pom.xml`） |
| MySQL | `services.mysql`，镜像 `mysql:8.0`，映射 `3306:3306`，健康检查 `mysqladmin ping -h 127.0.0.1 --silent`（该命令在鉴权失败时仍返回 0，因此健康检查里不需要写密码） |
| 数据库 | `MYSQL_DATABASE=fod`，`MYSQL_USER=fod_ci`，schema 由 `fod-backend/init.sql` 初始化 |
| 权限 | `chmod +x fod-backend/mvnw` |
| 执行 | `fod-backend` 目录下 `./mvnw test` |

步骤名称刻意区分「Set up JDK」「Wait for MySQL and initialize schema」「Run backend tests」，
失败时可直接从日志判断是数据库、环境还是测试失败。

`Wait for MySQL and initialize schema` 用真实凭据执行 `SELECT 1` 循环探测（最多 60 次 × 2s），
而不只依赖 `mysqladmin ping`，避免容器已启动但用户尚未创建时的误判；
超时会输出 `::error::MySQL did not become ready in time` 并退出。
初始化后打印 `SHOW TABLES` 结果，便于确认 schema 已建立。

### frontend 任务

| 项 | 内容 |
|---|---|
| 运行环境 | `ubuntu-latest`，`timeout-minutes: 15` |
| Node | `actions/setup-node@v4`，版本 `22`，`cache: npm`（依赖路径 `fod-frontend/package-lock.json`） |
| 执行 | `npm ci` → `npm run build` |

### 凭据处理

CI 只使用写在 workflow 内的**一次性测试值**（`fod_ci` / `ci_only_test_password` / `ci_only_...jwt-secret...`），
不引用任何真实密码、账号或 JWT 密钥，也不读取仓库内的 `application-local.properties`。

应用侧通过环境变量 `DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET` 注入，
`DB_URL` 使用 `application.properties` 中的默认值（`localhost:3306/fod`），与 service 容器端口映射一致。

## 验证方式

### 命令

```text
python -c "import yaml; yaml.safe_load(open('.github/workflows/ci.yml'))"
（静态校验 package.json 与 package-lock.json 同步）
cd fod-backend && ./mvnw test
cd fod-frontend && npm run build
```

### 结果

| 检查 | 结果 |
|---|---|
| workflow YAML 解析 | 通过；`jobs = [backend, frontend]`；`on = push/pull_request → main` |
| lockfile 同步（`npm ci` 的前提） | 通过；`lockfileVersion 3`，dependencies 4 项、devDependencies 9 项与 package.json 完全一致 |
| 后端测试 | `Tests run: 32, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS` |
| 前端构建 | `vue-tsc -b` 退出码 0；`vite build` `97 modules transformed`，`✓ built in 7.49s` |
| **GitHub Actions 首次运行（PR #2）** | **All checks have passed · 2 successful checks**；无与 base 分支的冲突 |

### 首次 CI 运行结论

PR #2（`https://github.com/Jiggale/FightOrDie-Web/pull/2`）页面显示：

```text
✔ All checks have passed
  2 successful checks
✔ No conflicts with base branch
```

workflow 中定义的两个 job 即这两个检查：`Backend · mvnw test` 与 `Frontend · npm run build`。
说明 MySQL 8 服务容器启动、`init.sql` 初始化、JDK 23 环境与 `mvnw test`、
以及 Node 22 下的 `npm ci` + `npm run build` 均在 GitHub 托管运行器上正常工作。

### 未能本地验证的部分

- GitHub Actions 的 service container（MySQL 8）无法在本地复现；此项已由 PR #2 的首次托管运行覆盖。
- 本地沙箱的安全删除策略拦截了 `npm ci` 的批量删除（一次性删除 500+ 文件被阻断），
  因此本地改用「静态校验 lockfile 同步 + `npm run build`」来覆盖同一条路径；
  中断的 `npm ci` 曾破坏本地 `node_modules`，已用 `npm install` 修复，仓库文件未受影响。
  而 `npm ci` 本身已由 PR #2 的 CI 实际通过，本地缺口的风险已消除。

## 可复现路径

1. 推送 `task-002-ci` 并在 GitHub 上对 `main` 发起 PR。
2. PR 页面应出现两个检查：`Backend · mvnw test`、`Frontend · npm run build`。
3. `Backend · mvnw test` 的日志应依次出现：
   `Set up JDK 23` → `Make mvnw executable` → `Ensure MySQL client` →
   `Wait for MySQL and initialize schema`（含 `MySQL is up and CI credentials are accepted.` 与表名列表）→
   `Run backend tests`（含 `Tests run: 32, Failures: 0, Errors: 0`）。
4. `Frontend · npm run build` 的日志应出现 `npm ci` 成功与 `✓ built in …`。
5. 故意制造失败时（例如改动某个测试断言），失败应只集中在 `Backend · mvnw test`，
   `Frontend · npm run build` 保持绿色，以证明两任务互不干扰。

## 已知问题

1. **CI 已首次运行通过，但仅验证了成功路径。** PR #2 的 2 个检查全绿，
   证明 service container、JDK 23、`npm ci`、schema 初始化均可正常工作；
   尚未验证的是失败路径（例如故意让某个测试失败时，日志能否清晰定位到具体环节）。
   该验证已作为「可复现路径」第 5 条留给后续确认，非本次阻塞项。
2. **`timeout-minutes` 属实施方补充项。** 任务要求未明确列出，但可避免工作流挂起占用 6 小时默认上限，
   已在此记录以便审查。
3. **`permissions: contents: read` 属实施方补充项。** 用于收紧默认令牌权限，不改变行为。
4. **`Ensure MySQL client` 步骤为防御性措施。** `ubuntu-latest` 通常已预装 `mysql` 客户端，
   该步骤仅在缺失时安装；如果运行环境已自带，会立即跳过，不增加明显耗时。

## 未完成或偏离任务包的内容

1. **PR 需由用户在网页创建。** 本地环境没有 `gh` CLI 也没有 GitHub Token，
   无法通过 API 创建 PR，只能推送分支并提供创建链接。分支推送与 PR 合并均未越权执行。
2. **未提交 `docs/游戏策划案-v1.md`。** 该文件仍为未跟踪状态，不属于本任务范围。

## 请求审查的问题

1. `backend` 与 `frontend` 拆成两个 job（并行、互不阻塞）是否符合预期？
   若希望前端构建在未改动前端时也被跳过，可再加 `paths` 过滤，但会削弱「每次 PR 都有完整检查」的保证。
2. MySQL 就绪探测使用真实凭据的 `SELECT 1` 循环，而不是 `mysqladmin ping`，是否需要保留这层冗余？
3. 是否需要在 CI 中额外加入 `mvn -q -DskipTests package` 之类的打包校验，还是当前只跑测试即可？
