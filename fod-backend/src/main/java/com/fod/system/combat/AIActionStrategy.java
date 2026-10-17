package com.fod.system.combat;

import com.fod.entity.Monster;
import com.fod.entity.Player;

import java.util.Random;

/**
 * AI 行动策略 - 工厂方法 + 策略模式
 * 根据怪物AI类型决定行动（AGGRESSIVE/DEFENSIVE/GUARD/BOSS）
 */
public class AIActionStrategy {

    private static final Random random = new Random();

    private AIActionStrategy() {}

    public static CombatResult executeAI(Monster monster, Player player) {
        return switch (monster.getAiType()) {
            case AGGRESSIVE -> aggressiveAction(monster, player);
            case DEFENSIVE -> defensiveAction(monster, player);
            case GUARD -> guardAction(monster, player);
            case BOSS -> bossAction(monster, player);
        };
    }

    private static CombatResult aggressiveAction(Monster monster, Player player) {
        int baseDamage = Math.max(monster.getAttack() - player.getEffectiveDefense(), 1);
        if (random.nextDouble() < 0.2) {
            int critDamage = (int) (baseDamage * 1.5);
            player.takeDamage(critDamage);
            return new CombatResult.Builder()
                    .damageToPlayer(critDamage)
                    .description(monster.getName() + " 发动了凶猛一击，造成 " + critDamage + " 点伤害！")
                    .build();
        }
        player.takeDamage(baseDamage);
        return new CombatResult.Builder()
                .damageToPlayer(baseDamage)
                .description(monster.getName() + " 攻击了你，造成 " + baseDamage + " 点伤害。")
                .build();
    }

    private static CombatResult defensiveAction(Monster monster, Player player) {
        int baseDamage = Math.max(monster.getAttack() - player.getEffectiveDefense(), 1);
        player.takeDamage(baseDamage);
        return new CombatResult.Builder()
                .damageToPlayer(baseDamage)
                .description(monster.getName() + " 谨慎地攻击了你，造成 " + baseDamage + " 点伤害。")
                .build();
    }

    private static CombatResult guardAction(Monster monster, Player player) {
        int baseDamage = Math.max(monster.getAttack() - player.getEffectiveDefense(), 1);
        baseDamage = Math.max(baseDamage / 2, 1);
        player.takeDamage(baseDamage);
        return new CombatResult.Builder()
                .damageToPlayer(baseDamage)
                .description(monster.getName() + " 守卫着领地，对你造成了 " + baseDamage + " 点伤害。")
                .build();
    }

    private static CombatResult bossAction(Monster monster, Player player) {
        double hpRatio = (double) monster.getHealth() / (monster.getHealth() + 1);
        int baseDamage = Math.max(monster.getAttack() - player.getEffectiveDefense(), 1);
        if (hpRatio < 0.3) {
            int rageDamage = (int) (baseDamage * 2.0);
            player.takeDamage(rageDamage);
            return new CombatResult.Builder()
                    .damageToPlayer(rageDamage)
                    .description("⚡ " + monster.getName() + " 进入狂暴状态！造成 " + rageDamage + " 点巨额伤害！")
                    .build();
        }
        player.takeDamage(baseDamage);
        return new CombatResult.Builder()
                .damageToPlayer(baseDamage)
                .description(monster.getName() + " 挥动巨爪，造成 " + baseDamage + " 点伤害。")
                .build();
    }
}
