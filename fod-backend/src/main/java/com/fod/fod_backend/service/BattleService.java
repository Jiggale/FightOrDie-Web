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

    private static final List<Monster> MONSTER_POOL = List.of(
            EntityFactory.createGoblin(),
            EntityFactory.createCaveSpider(),
            EntityFactory.createSkeleton()
    );

    public BattleService(StatsService statsService) {
        this.statsService = statsService;
    }

    public GameSession createBattle(String playerName) {
        Monster template = MONSTER_POOL.get(new Random().nextInt(MONSTER_POOL.size()));
        Monster monster = new Monster.Builder()
                .name(template.getName())
                .health(template.getHealth())
                .attack(template.getAttack())
                .defense(template.getDefense())
                .level(template.getLevel())
                .exp(template.getExperienceReward())
                .gold(template.getGoldReward())
                .aiType(template.getAiType())
                .build();
        for (var drop : template.getDropTable()) {
            monster.getDropTable().add(drop);
        }

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
        if (session.getCombatSystem().isCombatEnded()) {
            return session;
        }

        int hpBefore = session.getMonster().getHealth();
        int playerHpBefore = session.getPlayer().getHealth();

        List<String> messages = session.getCombatSystem().executeTurn(strategyIndex);
        session.incrementTurn();

        // 计算本回合伤害
        int damageToMonster = hpBefore - session.getMonster().getHealth();
        int damageToPlayer = playerHpBefore - session.getPlayer().getHealth();
        session.getPlayer().setHealth(Math.max(0, session.getPlayer().getHealth()));
        session.accumulateDamage(damageToMonster, damageToPlayer);

        // 战斗结束 → 持久化
        if (session.getCombatSystem().isCombatEnded()) {
            boolean playerWon = !session.getMonster().isAlive();
            try {
                statsService.recordBattleResult(session, playerWon,
                        session.getTotalDamageDealt(), session.getTotalDamageTaken());
                log.info("战斗结算持久化完成: sessionId={}, won={}", sessionId, playerWon);
            } catch (Exception e) {
                log.error("战斗结算持久化失败: sessionId={}", sessionId, e);
            }
        }

        return session;
    }

    public boolean tryFlee(String sessionId) {
        GameSession session = getSession(sessionId);
        return session.getCombatSystem().tryFlee(new Random());
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
