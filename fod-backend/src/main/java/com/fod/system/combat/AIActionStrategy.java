package com.fod.system.combat;

import com.fod.entity.Monster;
import com.fod.entity.Player;

import java.util.Random;

/**
 * AI 行动策略 - 工厂方法 + 策略模式
 * 根据怪物AI类型决定行动（AGGRESSIVE/DEFENSIVE/GUARD/BOSS）。
 * <p>
 * 本类只负责一次敌人行动的结算：防御减伤在这里生效，防御数值统一由
 * {@link Player#takeDamage(int, int)} 扣除，避免重复计算。
 */
public class AIActionStrategy {

    private static final Random random = new Random();
    private static final int DEFENSE_REDUCTION_PERCENT = 50;

    private AIActionStrategy() {}

    /**
     * 执行一次敌人行动。
     *
     * @param defending 玩家本回合是否处于防御姿态
     * @return 敌人行动结果，其 damageToPlayer 等于本次实际扣除的生命值
     */
    public static CombatResult executeAI(Monster monster, Player player, boolean defending) {
        int reductionPercent = defending ? DEFENSE_REDUCTION_PERCENT : 0;
        return switch (monster.getAiType()) {
            case AGGRESSIVE -> aggressiveAction(monster, player, reductionPercent);
            case DEFENSIVE -> defensiveAction(monster, player, reductionPercent);
            case GUARD -> guardAction(monster, player, reductionPercent);
            case BOSS -> bossAction(monster, player, reductionPercent);
        };
    }

    private static CombatResult aggressiveAction(Monster monster, Player player, int reductionPercent) {
        int rawDamage = monster.getAttack();
        if (random.nextDouble() < 0.2) {
            int critDamage = (int) (rawDamage * 1.5);
            int applied = player.takeDamage(critDamage, reductionPercent);
            return new CombatResult.Builder()
                    .damageToPlayer(applied)
                    .description(monster.getName() + " 发动了凶猛一击，造成 " + applied + " 点伤害！")
                    .build();
        }
        int applied = player.takeDamage(rawDamage, reductionPercent);
        return new CombatResult.Builder()
                .damageToPlayer(applied)
                .description(monster.getName() + " 攻击了你，造成 " + applied + " 点伤害。")
                .build();
    }

    private static CombatResult defensiveAction(Monster monster, Player player, int reductionPercent) {
        int applied = player.takeDamage(monster.getAttack(), reductionPercent);
        return new CombatResult.Builder()
                .damageToPlayer(applied)
                .description(monster.getName() + " 谨慎地攻击了你，造成 " + applied + " 点伤害。")
                .build();
    }

    private static CombatResult guardAction(Monster monster, Player player, int reductionPercent) {
        int rawDamage = Math.max(monster.getAttack() / 2, 1);
        int applied = player.takeDamage(rawDamage, reductionPercent);
        return new CombatResult.Builder()
                .damageToPlayer(applied)
                .description(monster.getName() + " 守卫着领地，对你造成了 " + applied + " 点伤害。")
                .build();
    }

    private static CombatResult bossAction(Monster monster, Player player, int reductionPercent) {
        int baseHealth = monster.getHealth() + 1;
        double hpRatio = (double) monster.getHealth() / baseHealth;
        if (hpRatio < 0.3) {
            int rageDamage = (int) (monster.getAttack() * 2.0);
            int applied = player.takeDamage(rageDamage, reductionPercent);
            return new CombatResult.Builder()
                    .damageToPlayer(applied)
                    .description("⚡ " + monster.getName() + " 进入狂暴状态！造成 " + applied + " 点巨额伤害！")
                    .build();
        }
        int applied = player.takeDamage(monster.getAttack(), reductionPercent);
        return new CombatResult.Builder()
                .damageToPlayer(applied)
                .description(monster.getName() + " 挥动巨爪，造成 " + applied + " 点伤害。")
                .build();
    }
}
