package com.fod.fod_backend.dto;

import java.util.Map;

/**
 * 创建战斗响应 DTO
 */
public record BattleCreatedResponse(
        String sessionId,
        BattleStateResponse.PlayerInfo player,
        BattleStateResponse.MonsterInfo monster,
        BurningStateResponse burning,
        Map<Integer, String> strategies
) {
}
