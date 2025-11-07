package com.soarclient.event.server;

import com.soarclient.event.Event;

public class ChatEvent extends Event {
    private final String message;

    public ChatEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
