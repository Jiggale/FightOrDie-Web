package com.fod.fod_backend.service;

import com.fod.entity.Monster;
import com.fod.entity.Player;
import com.fod.fod_backend.dto.BattleOutcome;
import com.fod.system.combat.CombatSystem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {

    private Monster newMonster() {
        return new Monster.Builder()
                .name("夺烬者").health(24).attack(4).defense(2).level(1).exp(20).gold(10)
                .aiType(Monster.AIType.DEFENSIVE)
                .build();
    }

    private GameSession newSession() {
        Player player = new Player.Builder().name("hero").health(100).attack(10).defense(5).level(1).build();
        Monster monster = newMonster();
        return new GameSession("sid00001", player, monster, new CombatSystem(player, monster));
    }

    @Test
    void newSessionStartsWithAgreedBurningValues() {
        GameSession session = newSession();

        assertEquals(5, session.getBurningState().getFlame());
        assertEquals(6, session.getBurningState().getMaxFlame());
        assertEquals(0, session.getBurningState().getEmber());
        assertEquals(1, session.getBurningState().getBurnPerTurn());
        assertFalse(session.isBattleEnded());
        assertEquals(BattleOutcome.ONGOING, session.getBattleOutcome());
    }

    @Test
    void completeVictoryReturnsTrueAndAwardsOnce() {
        GameSession session = newSession();
        session.getMonster().takeDamage(999);

        assertTrue(session.completeVictory());

        assertEquals(5, session.getBurningState().getEmber());
        assertEquals(20, session.getPlayer().getExperience());
        assertEquals(10, session.getPlayer().getGold());
        assertEquals(1, session.getPlayer().getKillCount("夺烬者"));
    }

    @Test
    void completeVictoryReturnsFalseWhenRepeated() {
        GameSession session = newSession();
        session.getMonster().takeDamage(999);

        assertTrue(session.completeVictory());
        assertFalse(session.completeVictory());
        assertFalse(session.completeVictory());

        assertEquals(5, session.getBurningState().getEmber());
        assertEquals(20, session.getPlayer().getExperience());
        assertEquals(10, session.getPlayer().getGold());
        assertEquals(1, session.getPlayer().getKillCount("夺烬者"));
    }

    @Test
    void completeVictoryRejectsLivingMonster() {
        GameSession session = newSession();

        assertFalse(session.completeVictory());

        assertEquals(0, session.getBurningState().getEmber());
        assertEquals(0, session.getPlayer().getExperience());
        assertEquals(0, session.getPlayer().getGold());
    }

    @Test
    void completeVictoryRejectsExtinguishedPlayer() {
        GameSession session = newSession();
        session.getMonster().takeDamage(999);
        for (int i = 0; i < 5; i++) {
            session.getBurningState().consumeTurn();
        }

        assertFalse(session.completeVictory());

        assertEquals(0, session.getBurningState().getEmber());
        assertEquals(0, session.getPlayer().getExperience());
    }

    @Test
    void victoryAndExtinguishingInSameTurnAwardsNothing() {
        GameSession session = newSession();
        session.getMonster().takeDamage(999);
        for (int i = 0; i < 5; i++) {
            session.getBurningState().consumeTurn();
        }

        assertEquals(BattleOutcome.EXTINGUISHED, session.getBattleOutcome());
        assertFalse(session.completeVictory());
        assertEquals(0, session.getBurningState().getEmber());
        assertEquals(0, session.getPlayer().getGold());
        assertEquals(0, session.getPlayer().getKillCount("夺烬者"));
    }

    @Test
    void recordMessagesStoresImmutableCopy() {
        GameSession session = newSession();
        List<String> mutable = new ArrayList<>();
        mutable.add("第一回合");

        session.recordMessages(mutable);
        mutable.add("事后追加");

        assertEquals(List.of("第一回合"), session.getLastMessages());
        assertThrows(UnsupportedOperationException.class, () -> session.getLastMessages().add("x"));
    }

    @Test
    void isBattleEndedBecomesTrueWhenExtinguished() {
        GameSession session = newSession();

        for (int i = 0; i < 5; i++) {
            session.getBurningState().consumeTurn();
        }

        assertTrue(session.isExtinguished());
        assertTrue(session.isBattleEnded());
        assertEquals(BattleOutcome.EXTINGUISHED, session.getBattleOutcome());
    }

    @Test
    void isBattleEndedBecomesTrueWhenCombatEnds() {
        GameSession session = newSession();
        CombatSystem combat = session.getCombatSystem();

        for (int i = 0; i < 10 && !combat.isCombatEnded(); i++) {
            combat.executeTurn(1);
        }

        assertTrue(combat.isCombatEnded());
        assertTrue(session.isBattleEnded());
        assertEquals(BattleOutcome.VICTORY, session.getBattleOutcome());
    }

    @Test
    void outcomeIsFledOnlyAfterMarkingFlee() {
        GameSession session = newSession();
        assertEquals(BattleOutcome.ONGOING, session.getBattleOutcome());

        session.markFled();

        assertEquals(BattleOutcome.FLED, session.getBattleOutcome());
    }
}
