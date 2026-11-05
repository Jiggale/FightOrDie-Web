package com.fod.fod_backend.controller;

import com.fod.entity.Monster;
import com.fod.entity.Player;
import com.fod.fod_backend.dto.ApiResponse;
import com.fod.fod_backend.dto.BattleCreatedResponse;
import com.fod.fod_backend.dto.BattleOutcome;
import com.fod.fod_backend.dto.BattleStateResponse;
import com.fod.fod_backend.dto.StartBattleRequest;
import com.fod.fod_backend.service.BattleService;
import com.fod.fod_backend.service.GameSession;
import com.fod.system.combat.CombatSystem;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BattleControllerTest {

    private Player newPlayer() {
        return new Player.Builder().name("hero").health(100).attack(10).defense(5).level(1).build();
    }

    private Monster newMonster() {
        return new Monster.Builder()
                .name("夺烬者").health(24).attack(4).defense(2).level(1).exp(20).gold(10)
                .aiType(Monster.AIType.AGGRESSIVE)
                .build();
    }

    private GameSession newSession(String sessionId) {
        Player player = newPlayer();
        Monster monster = newMonster();
        return new GameSession(sessionId, player, monster, new CombatSystem(player, monster));
    }

    @Test
    void startReturnsPlayerAndOngoingOutcomeForFirstFrame() {
        GameSession session = newSession("start001");
        BattleService battleService = mock(BattleService.class);
        when(battleService.createBattle("hero")).thenReturn(session);
        when(battleService.getStrategies()).thenReturn(CombatSystem.getAvailableStrategies());

        BattleController controller = new BattleController(battleService);
        ApiResponse<BattleCreatedResponse> response = controller.startBattle(new StartBattleRequest("hero"));

        assertNotNull(response.data());
        assertNotNull(response.data().player(), "开始响应必须包含玩家数据，供前端第一帧渲染");
        assertEquals(100, response.data().player().health());
        assertEquals(100, response.data().player().maxHealth());
        assertEquals(10, response.data().player().attack());
        assertEquals(5, response.data().player().defense());
        assertEquals(5, response.data().burning().flame());
        assertEquals(BattleOutcome.ONGOING, response.data().battleOutcome());
    }

    @Test
    void fleeReturnsCompleteBattleStateWithFledOutcome() {
        GameSession session = newSession("flee0001");
        session.getCombatSystem().tryFlee(new Random() {
            @Override
            public double nextDouble() {
                return 0.0;
            }
        });
        session.markFled();
        session.getBurningState().consumeTurn();

        BattleService battleService = mock(BattleService.class);
        when(battleService.tryFlee("flee0001")).thenReturn(session);

        BattleController controller = new BattleController(battleService);
        ApiResponse<BattleStateResponse> response = controller.flee("flee0001");

        assertEquals(200, response.code());
        assertNotNull(response.data());
        assertEquals(BattleOutcome.FLED, response.data().battleOutcome());
        assertEquals(false, response.data().playerWon(), "逃跑不是胜利");
        assertEquals(session.getBurningState().getFlame(), response.data().burning().flame());
        assertEquals(session.getBurningState().getMaxFlame(), response.data().burning().maxFlame());
        assertEquals(session.getBurningState().getEmber(), response.data().burning().ember());
        assertEquals(session.getBurningState().getBurnPerTurn(), response.data().burning().burnPerTurn());
    }
}
