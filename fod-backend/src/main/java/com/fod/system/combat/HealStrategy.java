package com.fod.system.combat;

import com.fod.entity.Monster;
import com.fod.entity.Player;

/**
 * 治疗策略 - 只恢复生命值，不再触发敌人行动。
 */
public class HealStrategy implements CombatStrategy {

    private static final int HEAL_AMOUNT = 30;

    @Override
    public CombatResult execute(Player player, Monster monster) {
        player.heal(HEAL_AMOUNT);

        return new CombatResult.Builder()
                .damageToMonster(0)
                .damageToPlayer(0)
                .healAmount(HEAL_AMOUNT)
                .description("你使用了治疗，恢复了 " + HEAL_AMOUNT + " 点生命值。")
                .build();
    }

    @Override
    public String getName() { return "治疗（恢复30HP）"; }
}
