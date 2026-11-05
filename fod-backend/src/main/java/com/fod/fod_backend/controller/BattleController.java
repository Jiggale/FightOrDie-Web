package com.fod.fod_backend.controller;

import com.fod.fod_backend.dto.*;
import com.fod.fod_backend.service.BattleService;
import com.fod.fod_backend.service.GameSession;
import org.springframework.web.bind.annotation.*;

/**
 * 战斗 REST API 控制器
 *
 * API 清单：
 *   POST /api/battle/start          创建战斗
 *   POST /api/battle/{id}/action    执行行动
 *   POST /api/battle/{id}/flee      尝试逃跑
 *   GET  /api/battle/{id}/state     查看状态
 *   GET  /api/strategies             可用策略列表
 */
@RestController
@RequestMapping("/api")
public class BattleController {

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    /** POST /api/battle/start - 创建战斗 */
    @PostMapping("/battle/start")
    public ApiResponse<BattleCreatedResponse> startBattle(@RequestBody StartBattleRequest request) {
        GameSession session = battleService.createBattle(request.playerName());

        var response = new BattleCreatedResponse(
                session.getSessionId(),
                BattleStateResponse.PlayerInfo.from(session.getPlayer()),
                BattleStateResponse.MonsterInfo.from(session.getMonster()),
                BurningStateResponse.from(session.getBurningState()),
                battleService.getStrategies());

        return ApiResponse.ok(response);
    }

    /** POST /api/battle/{sessionId}/action - 执行行动 */
    @PostMapping("/battle/{sessionId}/action")
    public ApiResponse<BattleStateResponse> executeAction(
            @PathVariable String sessionId,
            @RequestBody ActionRequest request) {
        GameSession session = battleService.executeTurn(sessionId, request.strategyIndex());
        return ApiResponse.ok(toStateResponse(session));
    }

    /** POST /api/battle/{sessionId}/flee - 尝试逃跑，返回完整战斗状态 */
    @PostMapping("/battle/{sessionId}/flee")
    public ApiResponse<BattleStateResponse> flee(@PathVariable String sessionId) {
        GameSession session = battleService.tryFlee(sessionId);
        return ApiResponse.ok(toStateResponse(session));
    }

    /** GET /api/battle/{sessionId}/state - 查看战斗状态 */
    @GetMapping("/battle/{sessionId}/state")
    public ApiResponse<BattleStateResponse> getState(@PathVariable String sessionId) {
        GameSession session = battleService.getSession(sessionId);
        return ApiResponse.ok(toStateResponse(session));
    }

    /** GET /api/strategies - 获取可用策略列表 */
    @GetMapping("/strategies")
    public ApiResponse<java.util.Map<Integer, String>> getStrategies() {
        return ApiResponse.ok(battleService.getStrategies());
    }

    /**
     * 统一的战斗状态映射。
     * 结束判定统一走 {@link GameSession#isBattleEnded()}，燃尽按战败呈现。
     */
    private BattleStateResponse toStateResponse(GameSession session) {
        boolean extinguished = session.isExtinguished();
        boolean playerWon = session.isBattleEnded()
                && !session.getMonster().isAlive()
                && !extinguished;

        return new BattleStateResponse(
                session.getSessionId(),
                BattleStateResponse.PlayerInfo.from(session.getPlayer()),
                BattleStateResponse.MonsterInfo.from(session.getMonster()),
                BurningStateResponse.from(session.getBurningState()),
                session.isBattleEnded(),
                playerWon,
                session.getLastMessages(),
                session.getTurn());
    }
}
