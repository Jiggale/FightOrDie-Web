package com.fod.fod_backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BattleServiceTest {

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
    void missingSessionStillThrowsIllegalArgument() {
        BattleService service = newService();

        assertThrows(IllegalArgumentException.class, () -> service.executeTurn("not-exist", 1));
        assertThrows(IllegalArgumentException.class, () -> service.tryFlee("not-exist"));
    }
}
