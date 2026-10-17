package com.fod.system.combat;

import com.fod.entity.Monster;
import com.fod.entity.Player;

import java.util.Random;

/**
 * 普通攻击策略
 */
public class NormalAttackStrategy implements CombatStrategy {

    private static final Random random = new Random();
    private static final double CRIT_CHANCE = 0.15;
    private static final double CRIT_MULTIPLIER = 2.0;
    private static final double DODGE_CHANCE = 0.10;

    @Override
    public CombatResult execute(Player player, Monster monster) {
        CombatResult.Builder resultBuilder = new CombatResult.Builder();

        int baseDamage = Math.max(player.getEffectiveAttack() - monster.getDefense(), 1);
        boolean isCritical = random.nextDouble() < CRIT_CHANCE;
        int damageToMonster = isCritical ? (int) (baseDamage * CRIT_MULTIPLIER) : baseDamage;
        monster.takeDamage(damageToMonster);

        StringBuilder desc = new StringBuilder();
        if (isCritical) desc.append("⚡ 暴击！");
        desc.append("你对 ").append(monster.getName()).append(" 造成了 ")
                .append(damageToMonster).append(" 点伤害。");

        resultBuilder.damageToMonster(damageToMonster).isCritical(isCritical).description(desc.toString());

        if (monster.isAlive()) {
            boolean playerDodged = random.nextDouble() < DODGE_CHANCE;
            if (playerDodged) {
                resultBuilder.isDodged(true).damageToPlayer(0)
                        .description(resultBuilder.build().getDescription() + " 你闪避了怪物的攻击！");
            } else {
                int monsterDamage = Math.max(monster.getAttack() - player.getEffectiveDefense(), 1);
                player.takeDamage(monsterDamage);
                resultBuilder.damageToPlayer(monsterDamage)
                        .description(resultBuilder.build().getDescription()
                                + " " + monster.getName() + " 对你造成了 " + monsterDamage + " 点伤害。");
            }
        }

        return resultBuilder.build();
    }

    @Override
    public String getName() { return "普通攻击"; }
}
