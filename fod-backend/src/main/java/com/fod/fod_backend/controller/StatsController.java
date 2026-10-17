package com.fod.fod_backend.controller;

import com.fod.entity.BattleHistory;
import com.fod.entity.PlayerStats;
import com.fod.fod_backend.dto.ApiResponse;
import com.fod.fod_backend.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据统计 REST API
 *
 * 端点：
 *   GET /api/stats/history/{playerName}    玩家历史
 *   GET /api/stats/player/{playerName}      玩家统计
 *   GET /api/stats/leaderboard              金币排行榜
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/history/{playerName}")
    public ApiResponse<List<BattleHistory>> getHistory(@PathVariable String playerName) {
        return ApiResponse.ok(statsService.getPlayerHistory(playerName));
    }

    @GetMapping("/player/{playerName}")
    public ApiResponse<PlayerStats> getPlayerStats(@PathVariable String playerName) {
        PlayerStats stats = statsService.getPlayerStats(playerName);
        if (stats == null) {
            return ApiResponse.notFound("玩家不存在: " + playerName);
        }
        return ApiResponse.ok(stats);
    }

    @GetMapping("/leaderboard")
    public ApiResponse<List<PlayerStats>> getLeaderboard() {
        return ApiResponse.ok(statsService.getLeaderboard(10));
    }
}
