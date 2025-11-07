package com.soarclient.event.client;

import com.soarclient.event.Event;

public class EventAttackYaw extends Event {
    private float yaw;

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public EventAttackYaw(float yaw) {
        this.yaw = yaw;
    }
}
