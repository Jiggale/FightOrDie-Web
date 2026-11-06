package com.fod.system.combat;

import com.fod.entity.Monster;
import com.fod.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatSystemTest {

    private Player newPlayer() {
        return new Player.Builder().name("hero").health(100).attack(10).defense(5).level(1).build();
    }

    private Monster newMonster() {
        return new Monster.Builder()
                .name("夺烬者")
                .health(24).attack(4).defense(2).level(1).exp(20).gold(10)
                .aiType(Monster.AIType.DEFENSIVE)
                .build();
    }

    @Test
    void defenseTurnLetsMonsterActExactlyOnce() {
        Player player = newPlayer();
        Monster monster = newMonster();
        CombatSystem system = new CombatSystem(player, monster);

        system.executeTurn(2);

        assertEquals(99, player.getHealth(), "一个完整回合只能承受一次敌人攻击");
        assertFalse(system.isCombatEnded());
    }

    @Test
    void normalAttackTurnLetsMonsterActExactlyOnce() {
        Player player = newPlayer();
        Monster monster = newMonster();
        CombatSystem system = new CombatSystem(player, monster);

        system.executeTurn(1);

        assertEquals(99, player.getHealth(), "普通攻击的一个回合内敌人只能行动一次");
        assertTrue(monster.getHealth() < 24, "玩家行动应造成伤害");
    }
}
