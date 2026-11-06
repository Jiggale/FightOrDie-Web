package com.fod.system.combat;

import com.fod.entity.Monster;
import com.fod.entity.Player;

import java.util.Random;

/**
 * 普通攻击策略 - 只结算玩家造成的伤害与暴击，不再触发敌人行动。
 */
public class NormalAttackStrategy implements CombatStrategy {

    private static final Random random = new Random();
    private static final double CRIT_CHANCE = 0.15;
    private static final double CRIT_MULTIPLIER = 2.0;

    @Override
    public CombatResult execute(Player player, Monster monster) {
        int baseDamage = Math.max(player.getEffectiveAttack() - monster.getDefense(), 1);
        boolean isCritical = random.nextDouble() < CRIT_CHANCE;
        int damageToMonster = isCritical ? (int) (baseDamage * CRIT_MULTIPLIER) : baseDamage;
        monster.takeDamage(damageToMonster);

        StringBuilder desc = new StringBuilder();
        if (isCritical) desc.append("⚡ 暴击！");
        desc.append("你对 ").append(monster.getName()).append(" 造成了 ")
                .append(damageToMonster).append(" 点伤害。");

        return new CombatResult.Builder()
                .damageToMonster(damageToMonster)
                .isCritical(isCritical)
                .description(desc.toString())
                .build();
    }

    @Override
    public String getName() { return "普通攻击"; }
}
