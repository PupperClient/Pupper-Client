package com.soarclient.event.client;

import com.soarclient.event.Event;

public class ServerJoinEvent extends Event {

    private final String address;

    public ServerJoinEvent(final String address) {
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }
}
