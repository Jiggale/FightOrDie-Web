# 实施交接

> 此文件由实施方在每次任务完成后填写。没有实现或没有测试证据时，不得标记为完成。

## 当前状态

TASK-001（燃烧生存核心）首轮实现完成，已按 REVIEW-001 的 3 个阻塞项返修，
后端 32 项测试通过、前端构建通过、关键 API 行为已复核，等待复审。

## 任务编号

TASK-001（返修轮次）

## 实现目标

在现有战斗系统上打通最小闭环：每回合消耗燃烧、胜利获得余烬、战斗页正确显示两种资源。
本轮返修目标为 REVIEW-001 的 3 个阻塞项与 4 项补充测试。

## 改动文件

### 本轮返修新增

- `fod-backend/src/main/java/com/fod/fod_backend/dto/BattleOutcome.java`（新建）

### 本轮返修修改

- `fod-backend/src/main/java/com/fod/fod_backend/service/GameSession.java`
- `fod-backend/src/main/java/com/fod/fod_backend/service/BattleService.java`
- `fod-backend/src/main/java/com/fod/fod_backend/controller/BattleController.java`
- `fod-backend/src/main/java/com/fod/fod_backend/dto/BattleStateResponse.java`
- `fod-backend/src/main/java/com/fod/fod_backend/dto/BattleCreatedResponse.java`
- `fod-frontend/src/api/game.ts`
- `fod-frontend/src/store/game.ts`
- `fod-frontend/src/views/BattleView.vue`
- `fod-backend/src/test/java/com/fod/fod_backend/service/GameSessionTest.java`
- `fod-backend/src/test/java/com/fod/fod_backend/service/BattleServiceTest.java`
- `fod-backend/src/test/java/com/fod/fod_backend/controller/BattleControllerTest.java`

### 首轮实现（已在上一个提交中）

- `fod-backend/src/main/java/com/fod/system/survival/BurningState.java`（新建）
- `fod-backend/src/main/java/com/fod/entity/Player.java`
- `fod-backend/src/main/java/com/fod/system/combat/{CombatSystem,NormalAttackStrategy,DefenseStrategy,HealStrategy,AIActionStrategy}.java`
- `fod-backend/src/main/java/com/fod/fod_backend/dto/BurningStateResponse.java`（新建）
- `fod-backend/src/test/java/com/fod/system/survival/BurningStateTest.java`（新建）
- `fod-backend/src/test/java/com/fod/entity/PlayerTest.java`（新建）
- `fod-backend/src/test/java/com/fod/system/combat/CombatSystemTest.java`（新建）

## 实现说明

### 返修 1：统一 BattleOutcome

新增 `com.fod.fod_backend.dto.BattleOutcome`：`ONGOING`、`VICTORY`、`DEFEATED`、`EXTINGUISHED`、`FLED`。

`GameSession.getBattleOutcome()` 按规格固定优先级计算：

```text
燃烧归零 → EXTINGUISHED
敌人死亡 → VICTORY
玩家死亡 → DEFEATED
逃跑成功 → FLED
否则     → ONGOING
```

- 新增 `GameSession.markFled()` / `isFled()`；`BattleService.tryFlee` 在「逃跑成功且未燃尽」时标记。
- `BattleCreatedResponse` 与 `BattleStateResponse` 均新增 `battleOutcome`。
- `playerWon` 现在由 `outcome == VICTORY` 派生，不再由布尔组合推断。
- 前端 `BattleView.vue` 的标题与描述直接映射 `battleOutcome`：
  `VICTORY` → 胜利、`DEFEATED` → 战败、`EXTINGUISHED` → 燃尽、`FLED` → 已撤退。
  「获得余烬 +5」仅在 `VICTORY` 显示，因此成功逃跑不会再呈现为战败。

### 返修 2：前端第一帧即显示玩家数据

- `BattleCreated` 新增 `player: Player` 与 `battleOutcome: BattleOutcome`。
- `store.start()` 设置 `this.player = created.player`，不再置 `null`。
- Pinia 新增 `battleOutcome` 状态（安全默认 `ONGOING`），从开始与行动响应水合，`reset()` 时复位。

### 返修 3：completeVictory 自检

`GameSession.completeVictory()` 现在返回 `boolean`，并在发奖前自行校验：

- 奖励未发放（`victoryRewarded`）；
- 敌人已死亡（`!monster.isAlive()`）；
- 玩家未燃尽（`!burningState.isExtinguished()`）。

任一条件不满足直接返回 `false`，不发余烬、经验、金币与击杀；满足时发奖并返回 `true`。

### 返修 4：补充测试与可注入随机源

- `BattleService.tryFlee(String)` 保留原签名，内部委托 `tryFlee(String, Random)`；
  测试通过覆盖 `nextDouble()` 的固定随机源区分成功（0.0）与失败（0.9），避免偶发失败。
- 新增测试覆盖：胜利与燃尽同回合不发奖、成功逃跑返回 `FLED`、失败逃跑保持 `ONGOING`、
  逃跑导致燃烧归零返回 `EXTINGUISHED`、开始响应包含玩家数据。

### 首轮实现要点（保留）

- `BurningState` 固定初始值并保证扣减下限；`Player.takeDamage` 返回实际扣除值并支持百分比减伤重载。
- 一个完整回合只触发一次敌人行动；`NormalAttackStrategy` 不再包含闪避与敌人伤害分支。
- `BattleStateResponse` 日志字段为 `log`；`BattleService.executeTurn` 在结算后统一消耗 1 点燃烧，燃尽优先于胜利。

## 验证方式

### 命令

```text
cd fod-backend && .\mvnw.cmd test
cd fod-frontend && npm run build
```

### 结果

| 命令 | 结果 |
|---|---|
| `cd fod-backend && .\mvnw.cmd test` | **Tests run: 32, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS** |
| `cd fod-frontend && npm run build` | vue-tsc + vite 构建通过，97 modules transformed，`✓ built in 2.87s` |

分项：

```text
BurningStateTest        5
PlayerTest              3
CombatSystemTest        2
GameSessionTest        10
BattleServiceTest       7
BattleControllerTest    2
FodBackendApplicationTests 3
```

## 可复现路径

后端以 8081 端口启动（避免占用 8080）：

```powershell
cd fod-backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

### 1. 开始响应包含玩家数据与 ONGOING

```text
POST /api/battle/start {"playerName":"fix-check"}

START outcome=ONGOING playerHp=100/100 atk=10 def=5 flame=5
```

### 2. 行动后保持 ONGOING

```text
POST /api/battle/{id}/action {"strategyIndex":1}

ACTION outcome=ONGOING won=False flame=4
```

### 3. 逃跑结局区分（连续逃跑直到成功）

```text
try1 outcome=ONGOING flame=3 ended=False
try2 outcome=ONGOING flame=2 ended=False
try3 outcome=FLED    flame=1 ended=True
```

成功逃跑返回 `FLED`，与 `DEFEATED` 明确区分。

### 4. 胜利与燃尽（首轮已复现，本轮由单测补充）

- 单测 `victoryAndExtinguishingInSameTurnAwardsNothing` 覆盖「敌人死亡 + 燃烧归零同回合 → 不发奖」。
- 单测 `completeVictoryRejectsLivingMonster` / `completeVictoryRejectsExtinguishedPlayer` 覆盖前置条件。

### 5. 浏览器 UI

首轮已验证胜利弹窗（获得余烬 +5、当前燃烧 3/6、当前余烬 5）与燃尽弹窗（燃尽、0/6、余烬 0）。

本轮**新增的「已撤退」文案尚未做浏览器目视验证**，仅通过 API 确认 `battleOutcome=FLED`。
请在战斗页点击「🏃 逃跑」直到成功，确认弹窗标题为「已撤退」且没有「获得余烬 +5」。

## 已知问题

1. **燃尽仅能通过非伤害行动触发。** 按规格固定数值（敌人 24/4/2，玩家 10 攻击），普通攻击 2—3 回合即胜利，
   火焰不会归零；燃尽需持续防御或治疗触发。
2. **闪避机制被移除。** 计划 Task 2 Step 4 明确要求移除 `NormalAttackStrategy` 的玩家闪避分支，
   原 10% 闪避暂不存在。如后续仍需要闪避，应在防御结算中重新引入。
3. **Mockito 在 JDK 23 输出自附加警告。** 测试通过，但控制台出现 “Mockito is currently self-attaching…”。
   未新增 agent 配置以免超出任务范围。
4. **会话未清理。** 会话仍保存在内存 `ConcurrentHashMap`，未做过期回收，与既有实现一致。
5. **前端无自动化测试框架。** “开始响应中的 player 被前端正确初始化”以
   `BattleControllerTest.startReturnsPlayerAndOngoingOutcomeForFirstFrame` 在契约层覆盖，
   前端赋值本身未做单元测试（项目目前没有 vitest 等依赖，且任务禁止新增依赖）。
6. **「已撤退」文案未做浏览器验证**，见可复现路径第 5 节。

## 未完成或偏离任务包的内容

1. **敌人固定为规格指定的「夺烬者」。** 计划 Task 3 的文件清单未包含敌人定义，但规格 §3/§6 明确要求；
   原随机怪物池需 6—9 回合击杀，火焰 5 点会先归零，无法完成“开战到胜利结算”验收。
   已记录为 `collab/OPEN_QUESTIONS.md` Q-007。
2. **`ACTIVE_TASK.md` 状态未由实施方改写。** 当前文本为“首轮实现已完成，等待返修”，
   与本轮返修完成的实际状态不完全一致；按 `AGENTS.md` 由用户或 Codex 维护任务状态。
3. **未提交仓库中其他未跟踪文件。** `.github/`、`AGENTS.md`、`collab/`、`docs/superpowers/`
   已按用户指令在独立提交中加入；`docs/游戏策划案-v1.md` 按指令保持未跟踪。

## 请求审查的问题

1. 敌人固定为「夺烬者」是否符合预期？若后续恢复随机怪物池，燃烧初始值或怪物数值应如何重新平衡？
2. 燃尽在正常战斗路径不可达（见已知问题 1），是否需要调整 V0 数值使玩家在正常攻击下也面临燃尽风险？
3. 前端缺少自动化测试框架，是否需要在本任务范围外单独引入（会新增依赖）？
4. 会话内存回收是否需要在本阶段处理？
