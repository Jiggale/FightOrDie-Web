package com.fod.system.combat;

import com.fod.entity.Monster;
import com.fod.entity.Player;

/**
 * 治疗策略 - 恢复30HP，同时承受怪物攻击
 */
public class HealStrategy implements CombatStrategy {

    private static final int HEAL_AMOUNT = 30;

    @Override
    public CombatResult execute(Player player, Monster monster) {
        player.heal(HEAL_AMOUNT);
        int monsterDamage = Math.max(monster.getAttack() - player.getEffectiveDefense(), 1);
        player.takeDamage(monsterDamage);

        return new CombatResult.Builder()
                .damageToMonster(0)
                .damageToPlayer(monsterDamage)
                .healAmount(HEAL_AMOUNT)
                .description("你使用了治疗，恢复了 " + HEAL_AMOUNT + " 点生命值。"
                        + " " + monster.getName() + " 对你造成了 " + monsterDamage + " 点伤害。")
                .build();
    }

    @Override
    public String getName() { return "治疗（恢复30HP）"; }
}
