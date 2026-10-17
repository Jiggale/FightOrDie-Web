package com.fod.system.event;

import com.fod.entity.Player;

public class PlayerEvent extends GameEvent {

    private final Player player;
    private final int oldValue;
    private final int newValue;

    public PlayerEvent(EventType type, String source, Player player, int oldValue, int newValue) {
        super(type, source);
        this.player = player;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Player getPlayer() { return player; }
    public int getOldValue() { return oldValue; }
    public int getNewValue() { return newValue; }

    public static PlayerEvent levelUp(Player player, int oldLevel, int newLevel) {
        return new PlayerEvent(EventType.PLAYER_LEVEL_UP, "Player", player, oldLevel, newLevel);
    }

    public static PlayerEvent damaged(Player player, int damage) {
        return new PlayerEvent(EventType.PLAYER_DAMAGED, "Combat", player, damage, player.getHealth());
    }

    public static PlayerEvent died(Player player) {
        return new PlayerEvent(EventType.PLAYER_DIED, "Combat", player, 0, 0);
    }
}
