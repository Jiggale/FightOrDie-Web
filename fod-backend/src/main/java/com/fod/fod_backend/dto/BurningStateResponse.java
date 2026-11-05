package com.fod.fod_backend.dto;

import com.fod.system.survival.BurningState;

/**
 * 燃烧状态响应 DTO - 只读快照，不参与业务计算。
 */
public record BurningStateResponse(
        int flame,
        int maxFlame,
        int ember,
        int burnPerTurn
) {
    public static BurningStateResponse from(BurningState state) {
        return new BurningStateResponse(
                state.getFlame(),
                state.getMaxFlame(),
                state.getEmber(),
                state.getBurnPerTurn());
    }
}
