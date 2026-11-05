# Burning Survival Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a verifiable flame-and-ember loop to the existing battle session without expanding into story or other game systems.

**Architecture:** Keep survival arithmetic in a pure Java value object. Store it in `GameSession`, update it in `BattleService` after a real combat round, expose a single authoritative battle outcome through battle DTOs, and display it in the existing Vue battle page.

**Tech Stack:** Java 23, Spring Boot, JUnit 5, Mockito, Vue 3, TypeScript, Pinia, Vite.

**Spec:** `docs/superpowers/specs/2026-11-05-burning-survival-core-design.md`

## Global Constraints

- Initial values are exactly `flame=5`, `maxFlame=6`, `ember=0`, `burnPerTurn=1`.
- The player enemy is not persisted to the database in this slice.
- Each completed combat round consumes exactly `1` flame.
- A victory awards exactly `5` ember once and only once.
- Failure or fleeing awards no ember.
- API JSON must use the field name `log`, matching `fod-frontend/src/api/game.ts`.
- API JSON must return one `battleOutcome` value: `ONGOING`, `VICTORY`, `DEFEATED`, `EXTINGUISHED`, or `FLED`.
- `GameSession.completeVictory()` must enforce its own victory preconditions.
- Do not add Ink, investigation, promises, equipment, progression, or additional screens.
- Follow existing Vue and Spring package organization.

## Review Focus

- A repeated action after combat ends must not drain flame or award ember again.
- Every completed player action must consume one flame before outcome settlement.
- If flame reaches zero, no victory reward is granted even when the enemy also dies.
- Existing combat strategies must not cause the monster to attack twice in one turn.
- Flee must return a complete battle state instead of a string.
- A successful flee must remain visibly distinct from defeat.
- The first rendered battle frame must use player data from the start response.
- Missing or malformed session IDs must continue to return the project's normal API error.

---

### Task 1: Burning State Value Object

**Files:**
- Create: `fod-backend/src/main/java/com/fod/system/survival/BurningState.java`
- Test: `fod-backend/src/test/java/com/fod/system/survival/BurningStateTest.java`

**Interfaces:**
- Consumes: no project services.
- Produces:
  - `static BurningState initial()`
  - `int getFlame()`
  - `int getMaxFlame()`
  - `int getEmber()`
  - `int getBurnPerTurn()`
  - `void consumeTurn()`
  - `void gainEmber(int amount)`
  - `boolean isExtinguished()`

- [ ] **Step 1: Write the failing tests**

Create `BurningStateTest` with:

```java
@Test
void initialStateUsesAgreedValues() {
    BurningState state = BurningState.initial();
    assertEquals(5, state.getFlame());
    assertEquals(6, state.getMaxFlame());
    assertEquals(0, state.getEmber());
    assertEquals(1, state.getBurnPerTurn());
}

@Test
void consumeTurnReducesFlameAndNeverGoesBelowZero() {
    BurningState state = BurningState.initial();
    for (int i = 0; i < 10; i++) state.consumeTurn();
    assertEquals(0, state.getFlame());
    assertTrue(state.isExtinguished());
}

@Test
void gainEmberRejectsNegativeAmounts() {
    BurningState state = BurningState.initial();
    assertThrows(IllegalArgumentException.class, () -> state.gainEmber(-1));
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd fod-backend && ./mvnw.cmd test -Dtest=BurningStateTest`

Expected: compilation failure because `BurningState` does not exist.

- [ ] **Step 3: Implement `BurningState`**

Create a final class with private mutable integer fields, the exact initial values from the spec, and the public methods listed above. `consumeTurn()` uses `Math.max(0, flame - burnPerTurn)`. `gainEmber()` throws for negative input and otherwise adds the amount.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd fod-backend && ./mvnw.cmd test -Dtest=BurningStateTest`

Expected: all `BurningStateTest` tests pass.

- [ ] **Step 5: Commit**

```bash
git add fod-backend/src/main/java/com/fod/system/survival/BurningState.java fod-backend/src/test/java/com/fod/system/survival/BurningStateTest.java
git commit -m "feat: add burning state model"
```

### Task 2: Deterministic Combat Round Semantics

**Files:**
- Modify: `fod-backend/src/main/java/com/fod/entity/Player.java`
- Modify: `fod-backend/src/main/java/com/fod/system/combat/CombatSystem.java`
- Modify: `fod-backend/src/main/java/com/fod/system/combat/NormalAttackStrategy.java`
- Modify: `fod-backend/src/main/java/com/fod/system/combat/DefenseStrategy.java`
- Modify: `fod-backend/src/main/java/com/fod/system/combat/HealStrategy.java`
- Modify: `fod-backend/src/main/java/com/fod/system/combat/AIActionStrategy.java`
- Test: `fod-backend/src/test/java/com/fod/entity/PlayerTest.java`
- Test: `fod-backend/src/test/java/com/fod/system/combat/CombatSystemTest.java`

**Interfaces:**
- Consumes: existing `Player`, `Monster`, and `CombatStrategy`.
- Produces:
  - `Player.takeDamage(int rawDamage): int`
  - `Player.takeDamage(int rawDamage, int reductionPercent): int`
  - one complete round means one player action plus at most one enemy action.

- [ ] **Step 1: Write failing damage tests**

Add a `PlayerTest` proving that raw damage is reduced by defense exactly once and is never lower than `1`. With 5 defense, raw damage `4` must return `1` and reduce HP from `100` to `99`. With a 50% reduction, raw damage `4` becomes `2` before defense, so it must return `1` and reduce HP to `99`.

Add a `CombatSystemTest` that creates a 100 HP player with 5 defense and a 24 HP monster with 4 attack. Execute defense once and assert that HP is `99`, not `98`; this proves the monster acts exactly once.

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd fod-backend && ./mvnw.cmd test -Dtest=PlayerTest,CombatSystemTest`

Expected: failure because the current flow applies the monster action twice.

- [ ] **Step 3: Make damage application return the actual amount**

Change `Player.takeDamage(int)` to return the actual applied damage. Add an overload accepting `reductionPercent` from `0` to `100`; reject values outside the range. Apply reduction to the raw attack first, then defense, then enforce a minimum of `1`.

- [ ] **Step 4: Separate player actions from the enemy response**

Change player strategies so they do not execute the normal monster attack inside the strategy. `NormalAttackStrategy` only deals player damage and reports a critical hit; remove its current player-dodge and enemy-damage branches. `HealStrategy` only heals. `DefenseStrategy` returns zero damage and only marks that the enemy response should be reduced.

In `CombatSystem`, after the player action and only when the monster survives, call `AIActionStrategy.executeAI(monster, player, defending)` exactly once. `defending` is true only for strategy index `2`. Apply the 50% defense reduction inside the AI damage calculation. The AI result's `damageToPlayer` must equal the value returned by `Player.takeDamage(...)`.

- [ ] **Step 5: Run the focused tests, then the full backend tests**

Run: `cd fod-backend && ./mvnw.cmd test -Dtest=PlayerTest,CombatSystemTest`

Expected: both test classes pass.

Run: `cd fod-backend && ./mvnw.cmd test`

Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add fod-backend/src/main/java/com/fod/entity/Player.java
git add fod-backend/src/main/java/com/fod/system/combat
git add fod-backend/src/test/java/com/fod/entity/PlayerTest.java
git add fod-backend/src/test/java/com/fod/system/combat/CombatSystemTest.java
git commit -m "fix: resolve each combat round once"
```

### Task 3: Attach Burning State to Battle Sessions

**Files:**
- Modify: `fod-backend/src/main/java/com/fod/system/combat/CombatSystem.java`
- Modify: `fod-backend/src/main/java/com/fod/fod_backend/service/GameSession.java`
- Modify: `fod-backend/src/main/java/com/fod/fod_backend/service/BattleService.java`
- Modify: `fod-backend/src/main/java/com/fod/fod_backend/dto/BattleCreatedResponse.java`
- Modify: `fod-backend/src/main/java/com/fod/fod_backend/dto/BattleStateResponse.java`
- Modify: `fod-backend/src/main/java/com/fod/fod_backend/controller/BattleController.java`
- Create: `fod-backend/src/main/java/com/fod/fod_backend/dto/BurningStateResponse.java`
- Test: `fod-backend/src/test/java/com/fod/fod_backend/service/GameSessionTest.java`
- Test: `fod-backend/src/test/java/com/fod/fod_backend/service/BattleServiceTest.java`
- Test: `fod-backend/src/test/java/com/fod/fod_backend/controller/BattleControllerTest.java`

**Interfaces:**
- Consumes: `BurningState.initial()` from Task 1.
- Produces:
  - `GameSession.getBurningState(): BurningState`
  - `GameSession.recordMessages(List<String>): void`
  - `GameSession.getLastMessages(): List<String>`
  - `GameSession.isExtinguished(): boolean`
  - `GameSession.isBattleEnded(): boolean`
  - `GameSession.completeVictory(): boolean` — returns `true` only when it awards the one-time victory; it must reject a living monster, an extinguished player, or an already rewarded session
  - `BattleService.tryFlee(String): GameSession`
  - `GameSession.getBattleOutcome(): BattleOutcome`
  - `BurningStateResponse(int flame, int maxFlame, int ember, int burnPerTurn)`
  - `BattleOutcome` enum with `ONGOING`, `VICTORY`, `DEFEATED`, `EXTINGUISHED`, `FLED`
  - DTO field `burning` in both battle response types
  - DTO field `battleOutcome`
  - DTO field `log`

- [ ] **Step 1: Write failing session tests**

Test that a new session starts with the agreed burning values, victory adds exactly 5 ember once, and calling `completeVictory()` again does not add more ember, experience, gold, or kills. Test that `completeVictory()` rejects a living monster and a session whose flame has reached zero. Test that `recordMessages()` stores an immutable copy. Test that `isBattleEnded()` becomes true when either the combat ends or flame reaches zero.

In `BattleServiceTest`, mock `StatsService`, execute one non-winning player action, and assert flame changed from `5` to `4`. Add a real normal-attack path that runs until the monster dies, asserting that flame equals `5 - completedTurns` and victory adds exactly `5` ember. Then execute another action and assert no additional flame or ember change. Add a test that a missing session identifier still throws `IllegalArgumentException`.

In `BattleControllerTest`, construct the controller with a mocked `BattleService` and verify that `flee()` returns an `ApiResponse<BattleStateResponse>` containing the same burning values and `FLED` outcome as the returned session. Do not require a database or start the full Spring context for this test.

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd fod-backend && ./mvnw.cmd test -Dtest=GameSessionTest`

Expected: compilation failure because the new session methods do not exist.

- [ ] **Step 3: Implement session and response changes**

Initialize one `BurningState` in `GameSession`, add an idempotent victory flag, and expose the new getters. `isBattleEnded()` must return `combatSystem.isCombatEnded() || burningState.isExtinguished()`. Create a shared `BurningStateResponse` record and include it in both start and action/state responses.

Final `BattleCreatedResponse` shape must be exactly:

```text
BattleCreatedResponse(
  String sessionId,
  BattleOutcome battleOutcome,
  BattleStateResponse.PlayerInfo player,
  BattleStateResponse.MonsterInfo monster,
  BurningStateResponse burning,
  Map<Integer, String> strategies
)
```

Remove the obsolete flat player fields from `BattleCreatedResponse`. Add `level` to `BattleCreatedResponse.MonsterInfo` or reuse `BattleStateResponse.MonsterInfo`. Rename the state response field `messages` to `log`. Add `battleOutcome` to every state response.

- [ ] **Step 4: Integrate turn consumption and victory rewards**

In `BattleService.executeTurn()`:

- return immediately if `session.isBattleEnded()`;
- execute the combat turn;
- consume exactly one burning turn;
- store the returned combat messages in `GameSession`;
- if flame reached zero, set `EXTINGUISHED`, do not reward victory, and do not evaluate lower-priority outcomes;
- otherwise, if the enemy died, set `VICTORY` and call `completeVictory()`;
- otherwise, if the player died, set `DEFEATED`;
- otherwise, retain `ONGOING`;
- persist battle statistics exactly as before, but report the player as defeated when the session is extinguished;
- construct `playerWon` from `combatEnded && !monster.isAlive() && !burningState.isExtinguished()`.

Move experience, gold, and kill recording out of `CombatSystem` and into `GameSession.completeVictory()`. `CombatSystem` may mark the monster as killed for event publication, but it must not mutate player rewards.

Change `BattleService.tryFlee()` to return the updated `GameSession`. A flee attempt consumes one burning turn whether it succeeds or fails. If that burn reaches zero, set `EXTINGUISHED`; otherwise a successful flee sets `FLED` and ends combat without granting ember. A failed non-extinguishing flee remains `ONGOING`. Change flee and state endpoints to return `BattleStateResponse` so their shape matches action responses.

All response builders in `BattleController` must use `session.isBattleEnded()` rather than checking only `combatSystem.isCombatEnded()`. On flee, append a message that states success or failure and the one-point burn cost. These messages must be present in the returned `log`.

- [ ] **Step 5: Run backend tests**

Run: `cd fod-backend && ./mvnw.cmd test`

Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add fod-backend/src/main/java fod-backend/src/test/java
git commit -m "feat: add burning resources to battle sessions"
```

### Task 4: Display Burning and Ember in Battle UI

**Files:**
- Modify: `fod-frontend/src/api/game.ts`
- Modify: `fod-frontend/src/store/game.ts`
- Modify: `fod-frontend/src/views/BattleView.vue`

**Interfaces:**
- Consumes: backend `BurningState` fields from Task 3.
- Produces: the battle screen displays `燃烧 X/6 · 每回合 -1 · 余烬 N` and preserves it after every action.

- [ ] **Step 1: Extend TypeScript API types**

Add:

```ts
export interface BurningState {
  flame: number
  maxFlame: number
  ember: number
  burnPerTurn: number
}
```

Add `burning: BurningState` to `BattleState` and `BattleCreated`, and change the state logs field to `log: string[]`.

Add `player: Player` to `BattleCreated`. Add the same `BattleOutcome` union to both response types.

- [ ] **Step 2: Extend the Pinia store**

Store `burning` and `battleOutcome` with safe defaults. Hydrate both from start and action responses. Set `player` from `created.player` during `start()` so the first battle frame renders HP, attack, and defense. Do not infer flame, ember, or the ending from other booleans in Vue components.

- [ ] **Step 3: Render the status strip**

In `BattleView.vue`, show flame, maximum flame, burn per turn, and ember above the action buttons. Show the fixed victory reward prediction `胜利后 +5 余烬` while combat is active.

- [ ] **Step 4: Handle the extinguished ending**

Map the ending label directly from `battleOutcome`: `VICTORY` → `胜利`, `DEFEATED` → `战败`, `EXTINGUISHED` → `燃尽`, `FLED` → `已撤退`. The settlement dialog must also show current flame and ember after victory.

- [ ] **Step 5: Run the frontend build**

Run: `cd fod-frontend && npm run build`

Expected: TypeScript compilation and Vite production build pass.

- [ ] **Step 6: Commit**

```bash
git add fod-frontend/src/api/game.ts fod-frontend/src/store/game.ts fod-frontend/src/views/BattleView.vue
git commit -m "feat: display burning survival state"
```

### Task 5: End-to-End Verification

**Files:**
- Modify: `collab/HANDOFF.md`

**Interfaces:**
- Consumes: the working backend and frontend.
- Produces: reproducible evidence for Codex review.

- [ ] **Step 1: Start the backend and frontend**

Run the commands from the repository README in separate terminals.

- [ ] **Step 2: Verify the happy path**

Start one battle and perform normal attacks until victory. Record the starting values, each round's flame value, the final ember value, and the displayed settlement text.

- [ ] **Step 3: Verify idempotency**

Request the state again after combat ends and attempt another action. Confirm flame and ember do not change again.

- [ ] **Step 4: Verify flee response**

Start a new battle, flee once, and confirm the client receives a complete battle state rather than a string.

- [ ] **Step 5: Complete the handoff**

Fill `collab/HANDOFF.md` with changed files, commands, pass/fail results, manual reproduction steps, screenshots if available, and any deviations from this plan.

- [ ] **Step 6: Commit**

```bash
git add collab/HANDOFF.md
git commit -m "docs: record burning survival handoff"
```
