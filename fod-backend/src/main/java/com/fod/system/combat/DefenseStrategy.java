package com.fod.system.combat;

import com.fod.entity.Monster;
import com.fod.entity.Player;

/**
 * 防御策略 - 自身不造成伤害，仅标志本回合进入防御姿态。
 * <p>
 * 实际的 50% 减伤由 {@link AIActionStrategy} 在结算敌人行动时生效，
 * 具体是否处于防御姿态由 {@link CombatSystem} 依据策略序号判定。
 */
public class DefenseStrategy implements CombatStrategy {

    @Override
    public CombatResult execute(Player player, Monster monster) {
        return new CombatResult.Builder()
                .damageToMonster(0)
                .damageToPlayer(0)
                .description("你进入防御姿态，准备承受攻击。")
                .build();
    }

    @Override
    public String getName() { return "防御"; }
}
