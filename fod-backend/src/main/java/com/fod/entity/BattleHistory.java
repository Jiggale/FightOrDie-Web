package com.fod.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 战斗历史记录实体
 */
@Data
public class BattleHistory {
    private Long id;
    private String sessionId;
    private String playerName;
    private String monsterName;
    private Boolean playerWon;
    private Integer expGained;
    private Integer goldGained;
    private Integer damageDealt;
    private Integer damageTaken;
    private Integer turns;
    private LocalDateTime finishedAt;
}
