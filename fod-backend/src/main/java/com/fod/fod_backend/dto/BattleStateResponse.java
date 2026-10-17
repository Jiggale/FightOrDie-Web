package com.fod.fod_backend.dto;

import com.fod.entity.Monster;
import com.fod.entity.Player;

import java.util.List;

/**
 * 战斗状态响应 DTO
 */
public record BattleStateResponse(
        String sessionId,
        PlayerInfo player,
        MonsterInfo monster,
        boolean combatEnded,
        boolean playerWon,
        List<String> messages,
        int turn
) {
    public record PlayerInfo(String name, int level, int health, int maxHealth,
                             int attack, int defense, int gold, int exp) {
        public static PlayerInfo from(Player p) {
            return new PlayerInfo(p.getName(), p.getLevel(), p.getHealth(),
                    p.getMaxHealth(), p.getEffectiveAttack(), p.getEffectiveDefense(),
                    p.getGold(), p.getExperience());
        }
    }

    public record MonsterInfo(String name, int level, int health,
                              int attack, int defense, String aiType) {
        public static MonsterInfo from(Monster m) {
            return new MonsterInfo(m.getName(), m.getLevel(), m.getHealth(),
                    m.getAttack(), m.getDefense(), m.getAiType().name());
        }
    }
}
