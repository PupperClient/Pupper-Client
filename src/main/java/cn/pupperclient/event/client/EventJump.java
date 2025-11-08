package cn.pupperclient.event.client;

import cn.pupperclient.event.Event;

public class EventJump extends Event {
    private float yaw;

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getYaw() {
        return this.yaw;
    }

    public EventJump(float yaw) {
        this.yaw = yaw;
    }
}
