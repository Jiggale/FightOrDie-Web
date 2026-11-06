# 审查记录

审查只判断是否满足已批准任务和验收标准，不借机重写设计。

## 审查模板

```markdown
## REVIEW-XXX / TASK-XXX

- 日期：
- 审查对象：
- 结论：通过 / 有条件通过 / 阻塞

### 通过项

### 阻塞项

### 建议项

### 证据

- 实际命令：
- 测试结果：
- 体验或截图路径：

### 需要用户决定
```

## 当前审查

## REVIEW-001 / TASK-001

- 日期：2026-11-05
- 审查对象：`feat/task-001-burning-core`，提交 `ae00ecc..ad42d5d`
- Pull Request：https://github.com/Jiggale/FightOrDie-Web/pull/1
- 对比基线：`main`（`e41da82`）
- 结论：**有条件通过，修复阻塞项后可合并**

### 通过项

- `BurningState` 的初始值、扣减下限和余烬累加符合规格。
- 战斗回合已改为玩家行动最多触发一次敌人行动，伤害不再重复计算。
- 战斗胜利余烬固定为 5，并通过会话内的幂等标记避免重复发放。
- 开始、行动、状态和逃跑接口均提供 `burning`，日志字段已统一为 `log`。
- 燃烧归零优先于胜利，正常业务路径不会发放胜利余烬。
- 战斗页已显示燃烧、余烬、每回合消耗和结算信息。
- 后端测试：24 项通过，0 失败。
- 前端 TypeScript 检查通过。

### 阻塞项

1. **逃跑成功会被前端显示成“战败”。**
   `applyState()` 只有 `combatEnded`、`playerWon` 和燃烧值，没有区分“逃跑成功”“被击败”“燃尽”；`BattleView.vue` 因此把成功逃跑显示为“战败 / 你倒下了”。

2. **开始战斗后的玩家状态没有传入前端 store。**
   后端 `BattleCreatedResponse` 已返回 `player`，但 `BattleCreated` 类型和 `start()` 没有接收它，`player` 仍被设为 `null`，导致玩家第一次行动前显示不出 HP、攻击和防御。

3. **胜利奖励的不变量只由调用方保证。**
   `GameSession.completeVictory()` 本身不检查怪物是否死亡或玩家是否燃尽；任意其他调用方都能在燃尽状态下错误发奖。应把保护条件放进该方法，并增加“胜利与燃尽同回合”的测试。

### 建议项

- 为成功逃跑、失败逃跑、逃跑导致燃尽分别增加测试；当前逃跑随机逻辑没有注入点，难以稳定覆盖。
- 前端固定写死“胜利后 +5 余烬”，建议后续由后端响应提供奖励值，避免规格调整时前后端失真。
- `HANDOFF.md` 如实记录了前端未经浏览器目视检查；合并前应完成一次浏览器验收。

### 证据

- 后端命令：`fod-backend\\mvnw.cmd test`
- 后端结果：`Tests run: 24, Failures: 0, Errors: 0`
- 前端类型检查：`node_modules\\.bin\\vue-tsc.cmd -b --force`
- 前端类型检查结果：退出码 0
- `npm run build`：本地审查环境在 Vite 调用 esbuild 时被进程权限阻止，未取得独立构建结果。

### 流程问题

- PR #1 已存在于 `https://github.com/Jiggale/FightOrDie-Web/pull/1`。
- PR 当前显示 `Checks 0`，尚未配置 GitHub Actions 自动验证。
- `AGENTS.md`、协作规则、规格、计划和本审查记录已生成在本地，但提交时本地 Git 元数据写入被环境权限阻止；需要由 CodeBuddy 或用户将这些文件加入 PR #1，并排除尚未归档的旧策划稿。

### 需要用户决定

- 是否让 CodeBuddy 修复 3 个阻塞项并补测试后再次提交审查。
- 是否将协作规则、任务包、规格和计划一并提交到本分支。

## REVIEW-002 / TASK-001 返修

- 日期：2026-11-06
- 审查对象：`feat/task-001-burning-core`，提交 `7b62408..fbb150e`
- Pull Request：https://github.com/Jiggale/FightOrDie-Web/pull/1
- 结论：**通过，可以合并**

### 通过项

- 上一轮的三个阻塞项均已修复。
- `BattleOutcome` 统一了 `ONGOING`、`VICTORY`、`DEFEATED`、`EXTINGUISHED` 和 `FLED`，前端不再通过布尔值猜测结局。
- 成功逃跑显示“已撤退”，不再显示为战败。
- 开始战斗响应中的 `player` 已进入 TypeScript 类型和 Pinia store，第一帧即可显示玩家数据。
- `GameSession.completeVictory()` 已自行校验敌人死亡和未燃尽，并返回是否真正发奖。
- 已覆盖胜利与燃尽同回合、成功逃跑、失败逃跑、逃跑导致燃尽等测试。
- 协作规则、规格、计划、审查记录和 GitHub 模板均已进入当前分支。

### 验证证据

- 后端：`fod-backend\mvnw.cmd test`
- 结果：`Tests run: 32, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`
- 前端：`vue-tsc -b --force` 退出码 0
- CodeBuddy 交接记录：Vite 构建通过，并在浏览器中确认“胜利 / 燃尽 / 已撤退”三种界面。
- Codex 环境仍受 esbuild 子进程权限限制，未独立复跑 Vite 产物构建。

### 非阻塞问题

1. `BattleOutcome` 位于 DTO 包，但由 service 层依赖；后续可移动到领域包。
2. `GameSession.isBattleEnded()` 未包含 `fled` 标记；当前调用路径会先结束战斗，因此目前不影响行为。
3. `playerWon` 与 `battleOutcome` 同时存在，存在信息重复；当前为兼容旧接口保留。
4. PR 目前仍是 `Checks 0`，需要后续增加 GitHub Actions。
5. GitHub 模板和部分 Markdown 存在行尾空格，不影响运行。

### 最终判断

Spec：通过。Standards：无阻塞问题。当前实现达到 TASK-001 的合并标准。
