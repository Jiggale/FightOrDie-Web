package com.fod.fod_backend.service;

import com.fod.entity.Monster;
import com.fod.entity.Player;
import com.fod.fod_backend.dto.BattleOutcome;
import com.fod.system.combat.CombatSystem;
import com.fod.system.survival.BurningState;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏会话 - 持有一次战斗的全部状态
 */
public class GameSession {

    /** 本阶段固定的胜利余烬奖励。 */
    public static final int VICTORY_EMBER_REWARD = 5;

    private final String sessionId;
    private final Player player;
    private final Monster monster;
    private final CombatSystem combatSystem;
    private final BurningState burningState;
    private int turn;
    private final long createdAt;
    private int totalDamageDealt;
    private int totalDamageTaken;
    private List<String> lastMessages = List.of();
    private boolean victoryRewarded;
    private boolean fled;

    public GameSession(String sessionId, Player player, Monster monster, CombatSystem combatSystem) {
        this.sessionId = sessionId;
        this.player = player;
        this.monster = monster;
        this.combatSystem = combatSystem;
        this.burningState = BurningState.initial();
        this.turn = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public void incrementTurn() { this.turn++; }

    public void accumulateDamage(int dealt, int taken) {
        this.totalDamageDealt += dealt;
        this.totalDamageTaken += taken;
    }

    /** 标记本场战斗以逃跑结束，用于区分“已撤退”与“战败”。 */
    public void markFled() { this.fled = true; }

    public boolean isFled() { return fled; }

    /** 保存最近一个回合的战斗日志，内部持有不可变副本。 */
    public void recordMessages(List<String> messages) {
        this.lastMessages = List.copyOf(new ArrayList<>(messages));
    }

    /**
     * 结算胜利奖励。
     * <p>
     * 自行校验前置条件：奖励未发放、敌人已死亡、玩家未燃尽。
     * 任一条件不满足时不得发奖。
     *
     * @return 本次是否真正发放了胜利奖励
     */
    public boolean completeVictory() {
        if (victoryRewarded || monster.isAlive() || burningState.isExtinguished()) {
            return false;
        }
        victoryRewarded = true;
        burningState.gainEmber(VICTORY_EMBER_REWARD);
        player.gainExperience(monster.getExperienceReward());
        player.addGold(monster.getGoldReward());
        player.recordKill(monster.getName());
        return true;
    }

    /** 燃烧归零。 */
    public boolean isExtinguished() {
        return burningState.isExtinguished();
    }

    /** 战斗是否已经结束：战斗本身结束或燃烧归零。 */
    public boolean isBattleEnded() {
        return combatSystem.isCombatEnded() || burningState.isExtinguished();
    }

    /**
     * 战斗结局，优先级：燃烧归零 → 敌人死亡 → 玩家死亡 → 逃跑成功 → 继续战斗。
     */
    public BattleOutcome getBattleOutcome() {
        if (burningState.isExtinguished()) {
            return BattleOutcome.EXTINGUISHED;
        }
        if (!monster.isAlive()) {
            return BattleOutcome.VICTORY;
        }
        if (!player.isAlive()) {
            return BattleOutcome.DEFEATED;
        }
        if (fled) {
            return BattleOutcome.FLED;
        }
        return BattleOutcome.ONGOING;
    }

    public boolean isVictoryRewarded() { return victoryRewarded; }

    public String getSessionId() { return sessionId; }
    public Player getPlayer() { return player; }
    public Monster getMonster() { return monster; }
    public CombatSystem getCombatSystem() { return combatSystem; }
    public BurningState getBurningState() { return burningState; }
    public List<String> getLastMessages() { return lastMessages; }
    public int getTurn() { return turn; }
    public long getCreatedAt() { return createdAt; }
    public int getTotalDamageDealt() { return totalDamageDealt; }
    public int getTotalDamageTaken() { return totalDamageTaken; }
}
