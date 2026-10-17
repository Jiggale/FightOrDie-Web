package com.fod.fod_backend.service;

import com.fod.entity.Monster;
import com.fod.entity.Player;
import com.fod.system.combat.CombatSystem;

/**
 * 游戏会话 - 持有一次战斗的全部状态
 */
public class GameSession {

    private final String sessionId;
    private final Player player;
    private final Monster monster;
    private final CombatSystem combatSystem;
    private int turn;
    private final long createdAt;
    private int totalDamageDealt;
    private int totalDamageTaken;

    public GameSession(String sessionId, Player player, Monster monster, CombatSystem combatSystem) {
        this.sessionId = sessionId;
        this.player = player;
        this.monster = monster;
        this.combatSystem = combatSystem;
        this.turn = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public void incrementTurn() { this.turn++; }

    public void accumulateDamage(int dealt, int taken) {
        this.totalDamageDealt += dealt;
        this.totalDamageTaken += taken;
    }

    public String getSessionId() { return sessionId; }
    public Player getPlayer() { return player; }
    public Monster getMonster() { return monster; }
    public CombatSystem getCombatSystem() { return combatSystem; }
    public int getTurn() { return turn; }
    public long getCreatedAt() { return createdAt; }
    public int getTotalDamageDealt() { return totalDamageDealt; }
    public int getTotalDamageTaken() { return totalDamageTaken; }
}
