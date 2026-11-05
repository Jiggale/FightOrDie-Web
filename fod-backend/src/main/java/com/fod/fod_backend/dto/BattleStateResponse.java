package com.fod.fod_backend.dto;

import com.fod.entity.Monster;
import com.fod.entity.Player;

import java.util.List;

/**
 * 战斗状态响应 DTO
 * <p>
 * 字段名 `log` 必须与前端 `fod-frontend/src/api/game.ts` 保持一致。
 * `battleOutcome` 是唯一的结局判定来源，前端不得用布尔值组合推断结局。
 */
public record BattleStateResponse(
        String sessionId,
        BattleOutcome battleOutcome,
        PlayerInfo player,
        MonsterInfo monster,
        BurningStateResponse burning,
        boolean combatEnded,
        boolean playerWon,
        List<String> log,
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
