# 实施交接

> 此文件由实施方在每次任务完成后填写。没有实现或没有测试证据时，不得标记为完成。

## 当前状态

TASK-001（燃烧生存核心）已实现，通过后端测试、前端构建与浏览器目视验证，等待审查。

## 任务编号

TASK-001

## 实现目标

在现有战斗系统上打通最小闭环：每回合消耗燃烧、胜利获得余烬、战斗页正确显示两种资源。

## 改动文件

后端：

- `fod-backend/src/main/java/com/fod/system/survival/BurningState.java`（新建）
- `fod-backend/src/main/java/com/fod/entity/Player.java`
- `fod-backend/src/main/java/com/fod/system/combat/CombatSystem.java`
- `fod-backend/src/main/java/com/fod/system/combat/NormalAttackStrategy.java`
- `fod-backend/src/main/java/com/fod/system/combat/DefenseStrategy.java`
- `fod-backend/src/main/java/com/fod/system/combat/HealStrategy.java`
- `fod-backend/src/main/java/com/fod/system/combat/AIActionStrategy.java`
- `fod-backend/src/main/java/com/fod/fod_backend/service/GameSession.java`
- `fod-backend/src/main/java/com/fod/fod_backend/service/BattleService.java`
- `fod-backend/src/main/java/com/fod/fod_backend/controller/BattleController.java`
- `fod-backend/src/main/java/com/fod/fod_backend/dto/BattleStateResponse.java`
- `fod-backend/src/main/java/com/fod/fod_backend/dto/BattleCreatedResponse.java`
- `fod-backend/src/main/java/com/fod/fod_backend/dto/BurningStateResponse.java`（新建）

后端测试：

- `fod-backend/src/test/java/com/fod/system/survival/BurningStateTest.java`（新建）
- `fod-backend/src/test/java/com/fod/entity/PlayerTest.java`（新建）
- `fod-backend/src/test/java/com/fod/system/combat/CombatSystemTest.java`（新建）
- `fod-backend/src/test/java/com/fod/fod_backend/service/GameSessionTest.java`（新建）
- `fod-backend/src/test/java/com/fod/fod_backend/service/BattleServiceTest.java`（新建）
- `fod-backend/src/test/java/com/fod/fod_backend/controller/BattleControllerTest.java`（新建）

前端：

- `fod-frontend/src/api/game.ts`
- `fod-frontend/src/store/game.ts`
- `fod-frontend/src/views/BattleView.vue`

文档：

- `collab/HANDOFF.md`
- `collab/OPEN_QUESTIONS.md`（追加 Q-007）

## 实现说明

### Task 1 燃烧状态值对象

`BurningState` 为纯 Java 值对象，固定初始值 `flame=5 / maxFlame=6 / ember=0 / burnPerTurn=1`。
`consumeTurn()` 使用 `Math.max(0, flame - burnPerTurn)`；`gainEmber()` 拒绝负数；`isExtinguished()` 判定 `flame <= 0`。

### Task 2 回合语义

- `Player.takeDamage(int)` 改为返回实际扣除的生命值；新增 `takeDamage(int, int reductionPercent)` 重载，越界抛 `IllegalArgumentException`。
- 伤害链路统一为：原始伤害 → 百分比减伤 → 扣防御 → 最低 1 点，实际扣除以生命值变化为准。
- `NormalAttackStrategy` 只结算玩家伤害与暴击，移除原玩家闪避分支与敌人伤害分支。
- `HealStrategy` 只治疗。
- `DefenseStrategy` 只返回零伤害并声明防御意图。
- `AIActionStrategy.executeAI(monster, player, defending)` 增加 `defending` 参数，50% 减伤在敌人行动结算内生效，`damageToPlayer` 等于 `Player.takeDamage(...)` 的返回值。
- `CombatSystem` 在玩家行动后、敌人存活时只调用一次 AI 行动；`defending` 仅在策略序号为 2 时为真。

### Task 3 会话与响应

- `GameSession` 持有 `BurningState`、`lastMessages`（不可变副本）、`victoryRewarded` 幂等标记。
- `isBattleEnded()` = `combatSystem.isCombatEnded() || burningState.isExtinguished()`。
- `completeVictory()` 幂等地发放 5 点余烬、怪物经验、怪物金币并记录击杀；经验/金币/击杀已从 `CombatSystem` 移出。
- `BattleService.executeTurn` 顺序：已结束直接返回 → 结算回合 → 消耗 1 点燃烧 → 记录日志与伤害 → 燃尽则不发奖 → 否则胜利则 `completeVictory()` → 战斗结束则持久化（燃尽按战败）。
- `BattleService.tryFlee` 返回 `GameSession`，无论成败消耗 1 点燃烧并写入日志，成功不发余烬。
- `BattleStateResponse` 字段 `messages` 重命名为 `log`，新增 `burning`；`BattleCreatedResponse` 改为 `(sessionId, player, monster, burning, strategies)`。
- `BattleController` 四个端点统一使用 `toStateResponse`，结束判定统一走 `session.isBattleEnded()`。

### Task 4 前端

- `game.ts` 新增 `BurningState` 接口，`BattleState`/`BattleCreated` 增加 `burning`，日志字段沿用 `log`。
- `store/game.ts` 保存 `burning`（安全默认值），从开始与行动响应中水合，组件不自行推算。
- `BattleView.vue` 在行动按钮上方显示 `🔥 燃烧 X/6 · 每回合 -1 · 余烬 N`，战斗未结束时显示 `胜利后 +5 余烬`；`flame <= 1` 时使用既有主题的 `brand` 高风险色，未使用硬编码色值。
- 结算弹窗根据状态显示 `胜利！` / `燃尽` / `战败`，并展示 `获得余烬 +5`、当前燃烧与当前余烬。

## 验证方式

### 命令

```text
cd fod-backend && .\mvnw.cmd test -Dtest=BurningStateTest
cd fod-backend && .\mvnw.cmd test -Dtest=PlayerTest,CombatSystemTest
cd fod-backend && .\mvnw.cmd test -Dtest=GameSessionTest,BattleServiceTest,BattleControllerTest
cd fod-backend && .\mvnw.cmd test
cd fod-frontend && npm run build
```

### 结果

| 命令 | 结果 |
|---|---|
| `-Dtest=BurningStateTest` | Tests run: 5, Failures: 0, Errors: 0 |
| `-Dtest=PlayerTest,CombatSystemTest` | Tests run: 5, Failures: 0, Errors: 0 |
| `-Dtest=GameSessionTest,BattleServiceTest,BattleControllerTest` | Tests run: 11, Failures: 0, Errors: 0 |
| `mvnw.cmd test`（全量） | Tests run: 24, Failures: 0, Errors: 0，BUILD SUCCESS |
| `npm run build` | vue-tsc + vite 构建通过，97 modules transformed |
| 浏览器目视（`http://localhost:5173/battle`） | 胜利与燃尽两条路径均符合预期，详见下方第 5、6 节 |

## 可复现路径

后端在 8081 端口启动（避免占用 8080）：

```powershell
cd fod-backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

### 1. 正常流程（胜利）

`POST /api/battle/start` body `{"playerName":"e2e-hero"}`，随后重复
`POST /api/battle/{id}/action` body `{"strategyIndex":1}`。

实测记录：

```text
START flame=5/6 ember=0 perTurn=1 monsterHp=24 atk=4 def=2 playerHp=100 atk=10 def=5
ROUND 1 flame=4 ember=0 monHp=16 plHp=99
ROUND 2 flame=3 ember=0 monHp=8  plHp=98
ROUND 3 flame=2 ember=5 monHp=0  plHp=98 ended=true won=true
FINAL ember=5 flame=2/6
```

### 2. 幂等

对已结束会话再次 `GET /state` 与两次 `POST /action`：

```text
STATE_BEFORE flame=2 ember=5 turn=3
ACTION_AFTER flame=2 ember=5 turn=3
ACTION_AGAIN flame=2 ember=5 turn=3
```

### 3. 逃跑

新建会话后 `POST /api/battle/{id}/flee`，响应 `data` 为对象而非字符串：

```text
DATA_TYPE=PSCustomObject
FLAME=4 EMBER=0 TURN=1 ENDED=true
HAS_BURNING=True HAS_LOG_ARRAY=True
```

### 4. 燃尽

新建会话后连续 5 次 `{"strategyIndex":2}`（防御，不造成伤害）：

```text
T1 flame=4 monHp=24
T2 flame=3 monHp=24
T3 flame=2 monHp=24
T4 flame=1 monHp=24
T5 flame=0 monHp=24 ended=true won=false
FINAL ember=0
```

### 5. 浏览器 UI —— 胜利路径

启动后端（IDEA，8080）与前端（`npm run dev`，5173），打开 `http://localhost:5173/battle`，
输入角色名后连续普通攻击至胜利。实测结算：

```text
标题      胜利！
击败      夺烬者
获得余烬  +5
当前燃烧  3/6
当前余烬  5
```

说明：本次战斗因首击暴击（16 伤害 + 8 伤害）在 2 回合结束，故火焰为 `5 - 2 = 3`，
与“通常需要三个回合”的规格一致范围内。

### 6. 浏览器 UI —— 燃尽路径

同一页面点「再战」后连续点击 5 次「防御」。实测结算：

```text
标题      燃尽
文案      燃烧耗尽，你化作了灰烬。
获得余烬  不显示（未发奖）
当前燃烧  0/6
当前余烬  0
```

## 已知问题

1. **燃尽仅能通过非伤害行动触发。** 按规格固定的敌人数值（24/4/2）与玩家（10 攻击）下，普通攻击 2—3 回合即胜利，火焰不会归零；燃尽路径需通过持续防御或治疗触发，已在路径 4、6 中复现。
2. **闪避机制被移除。** 计划 Task 2 Step 4 明确要求移除 `NormalAttackStrategy` 的玩家闪避分支，因此原 10% 闪避暂时不存在。如后续仍需要闪避，应在防御结算中重新引入。
3. **Mockito 在 JDK 23 输出自附加警告。** 测试通过，但控制台出现“Mockito is currently self-attaching…”。未新增 agent 配置，避免超出任务范围。
4. **会话未清理。** 会话仍保存在内存 `ConcurrentHashMap` 中，未做过期回收，与既有实现一致。

## 未完成或偏离任务包的内容

1. **敌人改为规格指定的「夺烬者」。** 计划 Task 3 的文件清单未包含敌人定义，但规格 §3 与 §6 明确要求敌为「夺烬者 HP24 / ATK4 / DEF2 / AGGRESSIVE」。原 `MONSTER_POOL` 随机池包含 HP40 蜘蛛与 HP50 骷髅，普通攻击需 6—9 回合，火焰 5 点必然先归零，无法完成“开始战斗到胜利结算”的验收。因此改为在 `BattleService.createBattle` 内构造固定敌人（该文件在 Task 3 修改清单内），并移除未再使用的 `MONSTER_POOL` 常量。已记录为 `OPEN_QUESTIONS.md` 的 Q-007。
2. **`ACTIVE_TASK.md` 状态未更新。** 文件仍标记“等待用户审核，尚未批准实施”，但用户已在会话中明确指示实施 TASK-001。按 `AGENTS.md` 用户拥有最终决策权，故予以执行。未擅自修改任务状态文件。
3. **提交了 `collab/OPEN_QUESTIONS.md`。** 计划 Task 5 的提交清单只列 `HANDOFF.md`；因按 `AGENTS.md` 要求追加了 Q-007，一并提交以便审查。
4. **未提交仓库中其他未跟踪文件。** `.github/`、`AGENTS.md`、`collab/`（除本次两个文档）、`docs/superpowers/`、`docs/游戏策划案-v1.md` 均保持未跟踪状态，不在本任务范围。

## 请求审查的问题

1. 敌人固定为「夺烬者」是否符合预期？若后续希望恢复随机怪物池，需要重新平衡燃烧初始值或怪物数值。
2. 燃尽在正常战斗路径中不可达（见已知问题 2），是否需要调整 V0 数值以便玩家在正常攻击下也会面临燃尽风险？
3. 会话结束后的内存回收是否需要在本阶段处理？
