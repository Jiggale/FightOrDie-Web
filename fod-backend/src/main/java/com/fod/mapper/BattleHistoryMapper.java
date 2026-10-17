package com.fod.mapper;

import com.fod.entity.BattleHistory;
import com.fod.entity.PlayerStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 战斗历史 + 玩家统计 Mapper
 */
@Mapper
public interface BattleHistoryMapper {

    /** 插入战斗历史 */
    int insertBattleHistory(BattleHistory history);

    /** 根据玩家名查询战斗历史 */
    List<BattleHistory> findByPlayerName(@Param("playerName") String playerName);

    /** 查询玩家统计（不存在则返回null） */
    PlayerStats findPlayerStats(@Param("playerName") String playerName);

    /** UPSERT 玩家统计 */
    int upsertPlayerStats(PlayerStats stats);

    /** 排行榜：按总金币排序 */
    List<PlayerStats> findTopByGold(@Param("limit") int limit);
}
