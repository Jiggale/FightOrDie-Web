package com.fod.fod_backend.dto;

import com.fod.entity.Monster;

import java.util.List;
import java.util.Map;

/**
 * 创建战斗响应 DTO
 */
public record BattleCreatedResponse(
        String sessionId,
        String playerName,
        int playerHealth,
        int playerMaxHealth,
        int playerAttack,
        int playerDefense,
        MonsterInfo monster,
        Map<Integer, String> strategies
) {
    public record MonsterInfo(String name, int health, int attack, int defense, String aiType) {}
}
