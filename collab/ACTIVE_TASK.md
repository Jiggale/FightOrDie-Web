# 当前任务

## TASK-001：燃烧生存核心

### 状态

**首轮实现已完成，Codex 审查发现 3 个阻塞问题，等待 CodeBuddy 返修。**

### 目标

在现有战斗系统上打通最小闭环：每回合消耗燃烧、胜利获得余烬、战斗页正确显示两种资源。

### 必读文件

- `docs/superpowers/specs/2026-11-05-burning-survival-core-design.md`
- `docs/superpowers/plans/2026-11-05-burning-survival-core.md`
- `collab/DECISIONS.md`

### 实施方式

1. 继续使用分支 `feat/task-001-burning-core` 和 PR #1，不新开分支。
2. 按最新规格和计划完成返修。
3. 每个逻辑修复补齐测试，并提交到当前分支。
4. 不实现剧情、Ink、非致命路线或契灵承诺；它们属于后续任务。
5. 完成后更新 `collab/HANDOFF.md` 并推送，让 PR #1 自动更新。

### 返修阻塞项

1. 增加统一 `BattleOutcome`，成功逃跑显示“已撤退”，不能显示为战败。
2. 前端从开始战斗响应初始化玩家数据，第一帧即显示 HP、攻击和防御。
3. `GameSession.completeVictory()` 自行检查怪物已死、玩家未燃尽和奖励未发放。
4. 补充胜利与燃尽同回合、成功逃跑、失败逃跑和逃跑导致燃尽的测试。

### 验收

以计划文件的“Global Constraints”“Review Focus”和每个 Task 的步骤为准。任何未覆盖的验收项都必须明确报告，不得口头声明完成。

### 禁止事项

- 不修改与本任务无关的排行榜、认证、数据库结构或视觉主题。
- 不增加新依赖，除非计划明确要求。
- 不自行修复未被测试覆盖的架构问题。
- 发现阻塞时停止实现，把问题写入 `collab/OPEN_QUESTIONS.md`。

## 完成后的交接要求

CodeBuddy 完成实现后必须更新 `collab/HANDOFF.md`，包括：

- 实现目标；
- 改动文件；
- 运行过的命令；
- 测试结果；
- 截图或可复现路径；
- 已知问题；
- 与任务包不一致或无法完成的部分。
