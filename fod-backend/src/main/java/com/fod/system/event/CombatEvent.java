package com.fod.system.event;

import com.fod.entity.Monster;

public class CombatEvent extends GameEvent {

    private final Monster monster;
    private final int damageDealt;
    private final boolean isCritical;
    private final boolean isDodged;

    public CombatEvent(EventType type, String source, Monster monster,
                       int damageDealt, boolean isCritical, boolean isDodged) {
        super(type, source);
        this.monster = monster;
        this.damageDealt = damageDealt;
        this.isCritical = isCritical;
        this.isDodged = isDodged;
    }

    public Monster getMonster() { return monster; }
    public int getDamageDealt() { return damageDealt; }
    public boolean isCritical() { return isCritical; }
    public boolean isDodged() { return isDodged; }

    public static CombatEvent monsterKilled(Monster monster) {
        return new CombatEvent(EventType.MONSTER_KILLED, "Combat", monster, 0, false, false);
    }

    public static CombatEvent criticalHit(Monster monster, int damage) {
        return new CombatEvent(EventType.CRITICAL_HIT, "Combat", monster, damage, true, false);
    }
}
