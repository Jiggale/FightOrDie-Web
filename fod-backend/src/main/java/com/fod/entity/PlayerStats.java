package com.fod.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 玩家统计实体
 */
@Data
public class PlayerStats {
    private Long id;
    private String playerName;
    private Integer totalBattles;
    private Integer totalWins;
    private Long totalExp;
    private Long totalGold;
    private Integer monstersKilled;
    private Integer highestLevel;
    private LocalDateTime lastBattleAt;
}
