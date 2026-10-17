package com.fod.system.combat;

import com.fod.entity.Monster;
import com.fod.entity.Player;

/**
 * 防御策略 - 减伤50%
 */
public class DefenseStrategy implements CombatStrategy {

    private static final double DEFENSE_REDUCTION = 0.5;

    @Override
    public CombatResult execute(Player player, Monster monster) {
        int monsterDamage = Math.max(
                (int) (monster.getAttack() - player.getEffectiveDefense() * (1 + DEFENSE_REDUCTION)), 1);
        player.takeDamage(monsterDamage);

        return new CombatResult.Builder()
                .damageToMonster(0)
                .damageToPlayer(monsterDamage)
                .description("你进入防御姿态，受到 " + monsterDamage + " 点伤害（减伤 " +
                        (int) (DEFENSE_REDUCTION * 100) + "%）。")
                .build();
    }

    @Override
    public String getName() { return "防御"; }
}
