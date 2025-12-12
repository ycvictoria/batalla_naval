package com.example.batallanaval.observer;

public class Event {

    private final EventType type;
    private final Object source;
    private final Object data;

    public Event(EventType type, Object source, Object data) {
        this.type = type;
        this.source = source;
        this.data = data;
    }

    public EventType getType() {
        return type;
    }

    public Object getSource() {
        return source;
    }

    public Object getData() {
        return data;
    }
}
