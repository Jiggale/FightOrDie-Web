package com.fod.fod_backend.dto;

import java.util.Map;

/**
 * 创建战斗响应 DTO
 * <p>
 * 必须携带 `player`，前端据此在第一帧就渲染 HP、攻击和防御。
 */
public record BattleCreatedResponse(
        String sessionId,
        BattleOutcome battleOutcome,
        BattleStateResponse.PlayerInfo player,
        BattleStateResponse.MonsterInfo monster,
        BurningStateResponse burning,
        Map<Integer, String> strategies
) {
}
