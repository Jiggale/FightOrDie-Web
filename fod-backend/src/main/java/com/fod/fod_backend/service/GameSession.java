package com.fod.fod_backend.service;

import com.fod.entity.Monster;
import com.fod.entity.Player;
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

    /** 保存最近一个回合的战斗日志，内部持有不可变副本。 */
    public void recordMessages(List<String> messages) {
        this.lastMessages = List.copyOf(new ArrayList<>(messages));
    }

    /** 结算胜利奖励，幂等：重复调用不会再次发放余烬、经验、金币或击杀记录。 */
    public void completeVictory() {
        if (victoryRewarded) {
            return;
        }
        victoryRewarded = true;
        burningState.gainEmber(VICTORY_EMBER_REWARD);
        player.gainExperience(monster.getExperienceReward());
        player.addGold(monster.getGoldReward());
        player.recordKill(monster.getName());
    }

    /** 燃烧归零。 */
    public boolean isExtinguished() {
        return burningState.isExtinguished();
    }

    /** 战斗是否已经结束：战斗本身结束或燃烧归零。 */
    public boolean isBattleEnded() {
        return combatSystem.isCombatEnded() || burningState.isExtinguished();
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
