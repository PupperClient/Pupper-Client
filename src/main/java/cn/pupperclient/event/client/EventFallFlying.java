package cn.pupperclient.event.client;

import cn.pupperclient.event.Event;

public class EventFallFlying extends Event {
    private float pitch;

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getPitch() {
        return this.pitch;
    }

    public EventFallFlying(float pitch) {
        this.pitch = pitch;
    }
}
