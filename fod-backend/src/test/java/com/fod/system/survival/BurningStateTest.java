package com.fod.system.survival;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BurningStateTest {

    @Test
    void initialStateUsesAgreedValues() {
        BurningState state = BurningState.initial();
        assertEquals(5, state.getFlame());
        assertEquals(6, state.getMaxFlame());
        assertEquals(0, state.getEmber());
        assertEquals(1, state.getBurnPerTurn());
    }

    @Test
    void consumeTurnReducesFlameAndNeverGoesBelowZero() {
        BurningState state = BurningState.initial();
        for (int i = 0; i < 10; i++) state.consumeTurn();
        assertEquals(0, state.getFlame());
        assertTrue(state.isExtinguished());
    }

    @Test
    void gainEmberRejectsNegativeAmounts() {
        BurningState state = BurningState.initial();
        assertThrows(IllegalArgumentException.class, () -> state.gainEmber(-1));
    }

    @Test
    void gainEmberAccumulates() {
        BurningState state = BurningState.initial();
        state.gainEmber(5);
        assertEquals(5, state.getEmber());
        state.gainEmber(1);
        assertEquals(6, state.getEmber());
    }

    @Test
    void isExtinguishedIsFalseBeforeFlameReachesZero() {
        BurningState state = BurningState.initial();
        for (int i = 0; i < 4; i++) state.consumeTurn();
        assertEquals(1, state.getFlame());
        assertFalse(state.isExtinguished());
    }
}
