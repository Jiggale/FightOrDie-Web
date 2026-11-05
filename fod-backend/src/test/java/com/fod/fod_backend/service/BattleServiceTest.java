package com.fod.fod_backend.service;

import com.fod.fod_backend.dto.BattleOutcome;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BattleServiceTest {

    /** 固定结果的随机源：0.0 必定逃跑成功，0.9 必定失败。 */
    private static Random fixedRandom(double value) {
        return new Random() {
            @Override
            public double nextDouble() {
                return value;
            }
        };
    }

    private BattleService newService() {
        return new BattleService(mock(StatsService.class));
    }

    @Test
    void oneNonWinningActionConsumesExactlyOneFlame() {
        BattleService service = newService();
        GameSession session = service.createBattle("hero");
        String id = session.getSessionId();

        assertEquals(5, session.getBurningState().getFlame());

        service.executeTurn(id, 1);

        assertEquals(4, session.getBurningState().getFlame());
        assertFalse(session.isBattleEnded());
        assertEquals(BattleOutcome.ONGOING, session.getBattleOutcome());
    }

    @Test
    void normalAttackPathReachesVictoryWithExactlyFiveEmber() {
        BattleService service = newService();
        GameSession session = service.createBattle("hero");
        String id = session.getSessionId();

        int guard = 0;
        while (!session.isBattleEnded() && guard++ < 20) {
            service.executeTurn(id, 1);
        }

        assertTrue(session.isBattleEnded());
        assertFalse(session.isExtinguished());
        assertFalse(session.getMonster().isAlive());
        assertEquals(BattleOutcome.VICTORY, session.getBattleOutcome());
        assertEquals(5, session.getBurningState().getEmber());
        assertEquals(5 - session.getTurn(), session.getBurningState().getFlame());
    }

    @Test
    void actionsAfterBattleEndDoNotDrainFlameOrAwardEmberAgain() {
        BattleService service = newService();
        GameSession session = service.createBattle("hero");
        String id = session.getSessionId();

        int guard = 0;
        while (!session.isBattleEnded() && guard++ < 20) {
            service.executeTurn(id, 1);
        }

        int flameAfter = session.getBurningState().getFlame();
        int emberAfter = session.getBurningState().getEmber();
        int turnAfter = session.getTurn();
        int expAfter = session.getPlayer().getExperience();
        int goldAfter = session.getPlayer().getGold();

        service.executeTurn(id, 1);
        service.executeTurn(id, 1);

        assertEquals(flameAfter, session.getBurningState().getFlame());
        assertEquals(emberAfter, session.getBurningState().getEmber());
        assertEquals(turnAfter, session.getTurn());
        assertEquals(expAfter, session.getPlayer().getExperience());
        assertEquals(goldAfter, session.getPlayer().getGold());
    }

    @Test
    void successfulFleeReturnsFledOutcome() {
        BattleService service = newService();
        GameSession session = service.createBattle("hero");

        service.tryFlee(session.getSessionId(), fixedRandom(0.0));

        assertTrue(session.isBattleEnded());
        assertEquals(BattleOutcome.FLED, session.getBattleOutcome());
        assertEquals(4, session.getBurningState().getFlame());
        assertEquals(0, session.getBurningState().getEmber());
    }

    @Test
    void failedFleeStaysOngoing() {
        BattleService service = newService();
        GameSession session = service.createBattle("hero");

        service.tryFlee(session.getSessionId(), fixedRandom(0.9));

        assertFalse(session.isBattleEnded());
        assertEquals(BattleOutcome.ONGOING, session.getBattleOutcome());
        assertEquals(4, session.getBurningState().getFlame());
    }

    @Test
    void fleeThatConsumesLastFlameReturnsExtinguished() {
        BattleService service = newService();
        GameSession session = service.createBattle("hero");
        for (int i = 0; i < 4; i++) {
            session.getBurningState().consumeTurn();
        }
        assertEquals(1, session.getBurningState().getFlame());

        service.tryFlee(session.getSessionId(), fixedRandom(0.0));

        assertEquals(BattleOutcome.EXTINGUISHED, session.getBattleOutcome());
        assertEquals(0, session.getBurningState().getFlame());
        assertEquals(0, session.getBurningState().getEmber());
    }

    @Test
    void missingSessionStillThrowsIllegalArgument() {
        BattleService service = newService();

        assertThrows(IllegalArgumentException.class, () -> service.executeTurn("not-exist", 1));
        assertThrows(IllegalArgumentException.class, () -> service.tryFlee("not-exist"));
    }
}
