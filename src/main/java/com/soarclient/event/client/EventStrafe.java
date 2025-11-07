package com.soarclient.event.client;

import com.soarclient.event.Event;

public class EventStrafe extends Event {
    private float yaw;

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getYaw() {
        return this.yaw;
    }

    public EventStrafe(float yaw) {
        this.yaw = yaw;
    }
}
