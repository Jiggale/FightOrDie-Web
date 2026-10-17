package com.fod.fod_backend.controller;

import com.fod.fod_backend.dto.*;
import com.fod.fod_backend.service.BattleService;
import com.fod.fod_backend.service.GameSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /** GET /api/battle/start?playerName=英雄 - 创建战斗（也支持POST） */
    @PostMapping("/battle/start")
    public ApiResponse<BattleCreatedResponse> startBattle(@RequestBody StartBattleRequest request) {
        GameSession session = battleService.createBattle(request.playerName());

        var response = new BattleCreatedResponse(
                session.getSessionId(),
                session.getPlayer().getName(),
                session.getPlayer().getHealth(),
                session.getPlayer().getMaxHealth(),
                session.getPlayer().getEffectiveAttack(),
                session.getPlayer().getEffectiveDefense(),
                new BattleCreatedResponse.MonsterInfo(
                        session.getMonster().getName(),
                        session.getMonster().getHealth(),
                        session.getMonster().getAttack(),
                        session.getMonster().getDefense(),
                        session.getMonster().getAiType().name()
                ),
                battleService.getStrategies()
        );

        return ApiResponse.ok(response);
    }

    /** POST /api/battle/{sessionId}/action - 执行行动 */
    @PostMapping("/battle/{sessionId}/action")
    public ApiResponse<BattleStateResponse> executeAction(
            @PathVariable String sessionId,
            @RequestBody ActionRequest request) {
        GameSession session = battleService.executeTurn(sessionId, request.strategyIndex());
        return ApiResponse.ok(new BattleStateResponse(
                sessionId,
                BattleStateResponse.PlayerInfo.from(session.getPlayer()),
                BattleStateResponse.MonsterInfo.from(session.getMonster()),
                session.getCombatSystem().isCombatEnded(),
                !session.getMonster().isAlive(),
                List.of("回合 " + session.getTurn() + " 完成"),
                session.getTurn()));
    }

    /** POST /api/battle/{sessionId}/flee - 尝试逃跑 */
    @PostMapping("/battle/{sessionId}/flee")
    public ApiResponse<String> flee(@PathVariable String sessionId) {
        boolean success = battleService.tryFlee(sessionId);
        return ApiResponse.ok(success ? "逃跑成功！" : "逃跑失败！");
    }

    /** GET /api/battle/{sessionId}/state - 查看战斗状态 */
    @GetMapping("/battle/{sessionId}/state")
    public ApiResponse<BattleStateResponse> getState(@PathVariable String sessionId) {
        GameSession session = battleService.getSession(sessionId);
        return ApiResponse.ok(new BattleStateResponse(
                sessionId,
                BattleStateResponse.PlayerInfo.from(session.getPlayer()),
                BattleStateResponse.MonsterInfo.from(session.getMonster()),
                session.getCombatSystem().isCombatEnded(),
                !session.getMonster().isAlive() && session.getCombatSystem().isCombatEnded(),
                List.of(),
                session.getTurn()));
    }

    /** GET /api/strategies - 获取可用策略列表 */
    @GetMapping("/strategies")
    public ApiResponse<java.util.Map<Integer, String>> getStrategies() {
        return ApiResponse.ok(battleService.getStrategies());
    }
}
