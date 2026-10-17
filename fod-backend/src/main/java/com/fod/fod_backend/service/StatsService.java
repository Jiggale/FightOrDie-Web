package com.fod.fod_backend.service;

import com.fod.entity.BattleHistory;
import com.fod.entity.Player;
import com.fod.entity.PlayerStats;
import com.fod.fod_backend.service.GameSession;
import com.fod.mapper.BattleHistoryMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计服务 - 战斗数据持久化 + Spring Cache 缓存加速
 */
@Service
public class StatsService {

    private final BattleHistoryMapper mapper;

    public StatsService(BattleHistoryMapper mapper) {
        this.mapper = mapper;
    }

    /** 结算战斗 + 清空相关缓存 */
    @Transactional
    @CacheEvict(value = {"leaderboard", "playerStats", "battleHistory"}, allEntries = true)
    public void recordBattleResult(GameSession session, boolean playerWon, int totalDamageDealt, int totalDamageTaken) {
        Player player = session.getPlayer();
        int exp = playerWon ? session.getMonster().getExperienceReward() : 0;
        int gold = playerWon ? session.getMonster().getGoldReward() : 0;

        BattleHistory history = new BattleHistory();
        history.setSessionId(session.getSessionId());
        history.setPlayerName(player.getName());
        history.setMonsterName(session.getMonster().getName());
        history.setPlayerWon(playerWon);
        history.setExpGained(exp);
        history.setGoldGained(gold);
        history.setDamageDealt(totalDamageDealt);
        history.setDamageTaken(totalDamageTaken);
        history.setTurns(session.getTurn());
        mapper.insertBattleHistory(history);

        PlayerStats stats = new PlayerStats();
        stats.setPlayerName(player.getName());
        stats.setTotalBattles(1);
        stats.setTotalWins(playerWon ? 1 : 0);
        stats.setTotalExp((long) exp);
        stats.setTotalGold((long) gold);
        stats.setMonstersKilled(playerWon ? 1 : 0);
        stats.setHighestLevel(player.getLevel());
        stats.setLastBattleAt(LocalDateTime.now());
        mapper.upsertPlayerStats(stats);
    }

    @Cacheable(value = "battleHistory", key = "#playerName")
    public List<BattleHistory> getPlayerHistory(String playerName) {
        return mapper.findByPlayerName(playerName);
    }

    @Cacheable(value = "playerStats", key = "#playerName")
    public PlayerStats getPlayerStats(String playerName) {
        return mapper.findPlayerStats(playerName);
    }

    @Cacheable(value = "leaderboard")
    public List<PlayerStats> getLeaderboard(int limit) {
        return mapper.findTopByGold(limit);
    }
}
