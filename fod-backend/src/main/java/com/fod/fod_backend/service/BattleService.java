package com.fod.fod_backend.service;

import com.fod.entity.Monster;
import com.fod.entity.Player;
import com.fod.system.combat.CombatSystem;
import com.fod.system.factory.EntityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 战斗服务 - 管理游戏会话和战斗逻辑
 * 战斗结束后自动调用 StatsService 持久化结果
 */
@Service
public class BattleService {

    private static final Logger log = LoggerFactory.getLogger(BattleService.class);
    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final StatsService statsService;
    private final Random random = new Random();

    public BattleService(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * 创建一场 V0 验证战斗。
     * 敌人固定为「夺烬者」：HP 24 / 攻击 4 / 防御 2，用于验证燃烧与余烬闭环。
     */
    public GameSession createBattle(String playerName) {
        Monster monster = new Monster.Builder()
                .name("夺烬者")
                .health(24)
                .attack(4)
                .defense(2)
                .level(1)
                .aiType(Monster.AIType.AGGRESSIVE)
                .build();

        Player player = EntityFactory.createDefaultPlayer(playerName);
        CombatSystem combatSystem = new CombatSystem(player, monster);

        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        GameSession session = new GameSession(sessionId, player, monster, combatSystem);
        sessions.put(sessionId, session);

        log.info("战斗会话创建: sessionId={}, player={}, monster={}", sessionId, playerName, monster.getName());
        return session;
    }

    public GameSession executeTurn(String sessionId, int strategyIndex) {
        GameSession session = getSession(sessionId);
        if (session.isBattleEnded()) {
            return session;
        }

        int monsterHpBefore = session.getMonster().getHealth();
        int playerHpBefore = session.getPlayer().getHealth();

        // 1. 结算玩家行动与可能的敌人反击
        List<String> messages = new ArrayList<>(session.getCombatSystem().executeTurn(strategyIndex));

        // 2. 无论双方是否死亡，本回合都算完成，消耗 1 点燃烧
        session.getBurningState().consumeTurn();
        session.incrementTurn();

        int damageToMonster = monsterHpBefore - session.getMonster().getHealth();
        int damageToPlayer = playerHpBefore - session.getPlayer().getHealth();
        session.getPlayer().setHealth(Math.max(0, session.getPlayer().getHealth()));
        session.accumulateDamage(damageToMonster, damageToPlayer);

        boolean extinguished = session.getBurningState().isExtinguished();
        boolean monsterDead = !session.getMonster().isAlive();

        if (extinguished) {
            messages.add("🔥 燃烧耗尽，你燃尽了。");
        }

        // 3. 燃尽优先：即使敌人同时死亡也不发放胜利奖励
        if (!extinguished && monsterDead) {
            session.completeVictory();
        }

        session.recordMessages(messages);

        // 4. 战斗结束 → 持久化（燃尽按战败记录）
        if (session.isBattleEnded()) {
            boolean playerWon = monsterDead && !extinguished;
            persistQuietly(session, playerWon);
        }

        return session;
    }

    public GameSession tryFlee(String sessionId) {
        return tryFlee(sessionId, random);
    }

    /**
     * 尝试逃跑。
     * <p>
     * 无论成功或失败都消耗 1 点燃烧；若该次消耗导致燃烧归零，结局为 {@code EXTINGUISHED}；
     * 否则成功逃跑不再发奖并结束战斗，结局为 {@code FLED}。
     *
     * @param random 逃跑成功率的随机源，便于测试注入
     */
    public GameSession tryFlee(String sessionId, Random random) {
        GameSession session = getSession(sessionId);
        if (session.isBattleEnded()) {
            return session;
        }

        boolean success = session.getCombatSystem().tryFlee(random);

        session.getBurningState().consumeTurn();
        session.incrementTurn();

        List<String> messages = new ArrayList<>();
        messages.add(success
                ? "🏃 逃跑成功，消耗 1 点燃烧。"
                : "🏃 逃跑失败，消耗 1 点燃烧。");

        boolean extinguished = session.getBurningState().isExtinguished();
        if (extinguished) {
            messages.add("🔥 燃烧耗尽，你燃尽了。");
        } else if (success) {
            session.markFled();
        }

        session.recordMessages(messages);
        return session;
    }

    private void persistQuietly(GameSession session, boolean playerWon) {
        try {
            statsService.recordBattleResult(session, playerWon,
                    session.getTotalDamageDealt(), session.getTotalDamageTaken());
            log.info("战斗结算持久化完成: sessionId={}, won={}", session.getSessionId(), playerWon);
        } catch (Exception e) {
            log.error("战斗结算持久化失败: sessionId={}", session.getSessionId(), e);
        }
    }

    public GameSession getSession(String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        return session;
    }

    public Map<Integer, String> getStrategies() {
        return CombatSystem.getAvailableStrategies();
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }
}
