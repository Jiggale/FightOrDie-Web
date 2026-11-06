package com.fod.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerTest {

    private Player newPlayer() {
        return new Player.Builder().name("hero").health(100).attack(10).defense(5).level(1).build();
    }

    @Test
    void takeDamageAppliesDefenseOnceAndKeepsMinimumOne() {
        Player player = newPlayer();

        int applied = player.takeDamage(4);

        assertEquals(1, applied);
        assertEquals(99, player.getHealth());
    }

    @Test
    void takeDamageAppliesReductionBeforeDefense() {
        Player player = newPlayer();

        int applied = player.takeDamage(4, 50);

        assertEquals(1, applied);
        assertEquals(99, player.getHealth());
    }

    @Test
    void takeDamageRejectsReductionPercentOutOfRange() {
        Player player = newPlayer();

        assertThrows(IllegalArgumentException.class, () -> player.takeDamage(4, -1));
        assertThrows(IllegalArgumentException.class, () -> player.takeDamage(4, 101));
    }
}
