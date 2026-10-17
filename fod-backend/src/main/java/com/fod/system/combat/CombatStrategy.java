package com.fod.system.combat;

import com.fod.entity.Player;
import com.fod.entity.Monster;

/**
 * 战斗策略接口 - 策略模式
 */
public interface CombatStrategy {
    CombatResult execute(Player player, Monster monster);
    String getName();
}
