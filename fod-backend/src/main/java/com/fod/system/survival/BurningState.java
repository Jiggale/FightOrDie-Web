package com.fod.system.survival;

/**
 * 燃烧状态 - 只负责燃烧数值、余烬累积与回合消耗。
 * <p>
 * V0 阶段的固定初始值：flame=5, maxFlame=6, ember=0, burnPerTurn=1。
 * 该类不感知战斗流程，也不负责奖励发放时机。
 */
public final class BurningState {

    private static final int INITIAL_FLAME = 5;
    private static final int INITIAL_MAX_FLAME = 6;
    private static final int INITIAL_EMBER = 0;
    private static final int INITIAL_BURN_PER_TURN = 1;

    private int flame;
    private final int maxFlame;
    private int ember;
    private final int burnPerTurn;

    private BurningState(int flame, int maxFlame, int ember, int burnPerTurn) {
        this.flame = flame;
        this.maxFlame = maxFlame;
        this.ember = ember;
        this.burnPerTurn = burnPerTurn;
    }

    /** 创建一个使用约定初始值的燃烧状态。 */
    public static BurningState initial() {
        return new BurningState(INITIAL_FLAME, INITIAL_MAX_FLAME, INITIAL_EMBER, INITIAL_BURN_PER_TURN);
    }

    /** 结束一个完整战斗回合，消耗燃烧值，最低为零。 */
    public void consumeTurn() {
        this.flame = Math.max(0, flame - burnPerTurn);
    }

    /** 增加余烬，负数视为非法输入。 */
    public void gainEmber(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("余烬数量不能为负数: " + amount);
        }
        this.ember += amount;
    }

    /** 燃烧归零即为燃尽。 */
    public boolean isExtinguished() {
        return flame <= 0;
    }

    public int getFlame() {
        return flame;
    }

    public int getMaxFlame() {
        return maxFlame;
    }

    public int getEmber() {
        return ember;
    }

    public int getBurnPerTurn() {
        return burnPerTurn;
    }
}
