package com.fod.system.event;

public class SimpleGameEvent extends GameEvent {

    public SimpleGameEvent(EventType type, String source) {
        super(type, source);
    }

    public static SimpleGameEvent of(EventType type, String source) {
        return new SimpleGameEvent(type, source);
    }
}
