package com.fod.fod_backend.dto;

/**
 * 战斗结局 - 唯一权威判定，前端不得根据多个布尔值自行猜测。
 * <p>
 * 判定优先级：燃烧归零 → 敌人死亡 → 玩家死亡 → 逃跑成功 → 继续战斗。
 */
public enum BattleOutcome {
    /** 战斗尚未结束。 */
    ONGOING,
    /** 玩家胜利。 */
    VICTORY,
    /** 玩家被敌人击败。 */
    DEFEATED,
    /** 燃烧归零。 */
    EXTINGUISHED,
    /** 玩家成功逃跑。 */
    FLED
}
