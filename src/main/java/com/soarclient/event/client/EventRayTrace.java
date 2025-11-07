package com.soarclient.event.client;

import com.soarclient.event.Event;
import net.minecraft.entity.Entity;

public class EventRayTrace extends Event{
    public Entity entity;
    public float yaw;
    public float pitch;
    private boolean modified = false;

    public EventRayTrace(Entity entity, float yaw, float pitch) {
        this.entity = entity;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        this.modified = true;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        this.modified = true;
    }

    public boolean isModified() {
        return modified;
    }
}
