package com.fod.system.event;

/**
 * 游戏事件基类 - 事件驱动架构的核心
 */
public abstract class GameEvent {

    public enum EventType {
        PLAYER_LEVEL_UP, PLAYER_DAMAGED, PLAYER_HEALED, PLAYER_DIED,
        COMBAT_START, COMBAT_END, COMBAT_TURN, MONSTER_KILLED, MONSTER_DAMAGED,
        CRITICAL_HIT, DODGE_EVENT,
        ITEM_FOUND, SCENE_ENTERED, SCENE_EXITED, QUEST_ACCEPTED, QUEST_COMPLETED,
        ACHIEVEMENT_UNLOCKED, MILESTONE_REACHED,
        GAME_START, GAME_SAVE, GAME_LOAD, GAME_EXIT, ERROR_EVENT
    }

    private final EventType type;
    private final long timestamp;
    private final String source;

    public GameEvent(EventType type, String source) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.source = source;
    }

    public EventType getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public String getSource() { return source; }

    @Override
    public String toString() {
        return "GameEvent{" + "type=" + type + ", timestamp=" + timestamp + ", source='" + source + '\'' + '}';
    }
}
