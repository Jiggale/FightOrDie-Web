package com.fod.fod_backend.controller;

import com.fod.entity.Monster;
import com.fod.entity.Player;
import com.fod.fod_backend.dto.ApiResponse;
import com.fod.fod_backend.dto.BattleStateResponse;
import com.fod.fod_backend.service.BattleService;
import com.fod.fod_backend.service.GameSession;
import com.fod.system.combat.CombatSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BattleControllerTest {

    @Test
    void fleeReturnsCompleteBattleStateWithBurning() {
        Player player = new Player.Builder().name("hero").health(100).attack(10).defense(5).level(1).build();
        Monster monster = new Monster.Builder()
                .name("夺烬者").health(24).attack(4).defense(2).level(1).exp(20).gold(10)
                .aiType(Monster.AIType.AGGRESSIVE)
                .build();
        GameSession session = new GameSession("flee0001", player, monster, new CombatSystem(player, monster));
        session.getBurningState().consumeTurn();

        BattleService battleService = mock(BattleService.class);
        when(battleService.tryFlee("flee0001")).thenReturn(session);

        BattleController controller = new BattleController(battleService);
        ApiResponse<BattleStateResponse> response = controller.flee("flee0001");

        assertEquals(200, response.code());
        assertNotNull(response.data());
        assertEquals(session.getBurningState().getFlame(), response.data().burning().flame());
        assertEquals(session.getBurningState().getMaxFlame(), response.data().burning().maxFlame());
        assertEquals(session.getBurningState().getEmber(), response.data().burning().ember());
        assertEquals(session.getBurningState().getBurnPerTurn(), response.data().burning().burnPerTurn());
    }
}
