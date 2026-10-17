package com.fod.system.combat;

import com.fod.entity.Monster;
import com.fod.entity.Player;
import com.fod.system.event.CombatEvent;
import com.fod.system.event.EventBus;
import com.fod.system.event.GameEvent;
import com.fod.system.event.PlayerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 战斗系统 - 策略模式 + 事件驱动
 * 负责战斗流程编排，支持选择不同战斗策略
 */
public class CombatSystem {

    private static final Logger log = LoggerFactory.getLogger(CombatSystem.class);
    private static final EventBus eventBus = EventBus.getInstance();
    private static final Map<Integer, CombatStrategy> strategyMap = new LinkedHashMap<>();

    static {
        strategyMap.put(1, new NormalAttackStrategy());
        strategyMap.put(2, new DefenseStrategy());
        strategyMap.put(3, new HealStrategy());
    }

    private final Player player;
    private final Monster monster;
    private boolean combatEnded = false;

    public CombatSystem(Player player, Monster monster) {
        this.player = player;
        this.monster = monster;
        eventBus.publish(new CombatEvent(
                GameEvent.EventType.COMBAT_START, "CombatSystem", monster, 0, false, false));
    }

    /**
     * 执行一个完整的战斗回合
     * @param strategyIndex 1=攻击 2=防御 3=治疗
     * @return 战斗描述列表
     */
    public List<String> executeTurn(int strategyIndex) {
        if (combatEnded) return List.of("战斗已经结束。");

        List<String> messages = new ArrayList<>();

        // 玩家行动
        CombatStrategy strategy = strategyMap.getOrDefault(strategyIndex, strategyMap.get(1));
        CombatResult playerResult = strategy.execute(player, monster);
        messages.add(playerResult.getDescription());

        if (playerResult.isCritical()) {
            eventBus.publish(CombatEvent.criticalHit(monster, playerResult.getDamageToMonster()));
        }

        // 检查怪物死亡
        if (!monster.isAlive()) {
            endCombat(true);
            messages.add("🏆 你击败了 " + monster.getName() + "！");
            messages.add("获得 " + monster.getExperienceReward() + " 经验值和 " +
                    monster.getGoldReward() + " 金币。");
            player.gainExperience(monster.getExperienceReward());
            player.addGold(monster.getGoldReward());
            player.recordKill(monster.getName());
            eventBus.publish(CombatEvent.monsterKilled(monster));
            return messages;
        }

        // 怪物AI行动
        CombatResult aiResult = AIActionStrategy.executeAI(monster, player);
        messages.add(aiResult.getDescription());

        // 检查玩家死亡
        if (!player.isAlive()) {
            endCombat(false);
            messages.add("💀 你被 " + monster.getName() + " 击败了...");
            eventBus.publish(PlayerEvent.died(player));
        }

        return messages;
    }

    public boolean tryFlee(Random random) {
        boolean success = random.nextDouble() < 0.5;
        if (success) endCombat(false);
        return success;
    }

    private void endCombat(boolean playerWon) {
        combatEnded = true;
        eventBus.publish(new CombatEvent(
                GameEvent.EventType.COMBAT_END, "CombatSystem", monster, 0, false, false));
    }

    public static Map<Integer, String> getAvailableStrategies() {
        Map<Integer, String> display = new LinkedHashMap<>();
        strategyMap.forEach((k, v) -> display.put(k, v.getName()));
        return display;
    }

    public boolean isCombatEnded() { return combatEnded; }
    public Monster getMonster() { return monster; }
}
