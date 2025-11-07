package com.soarclient.event.client;

import com.soarclient.event.Event;
import com.soarclient.event.EventType;
import net.minecraft.network.packet.Packet;

public class EventGlobalPacket extends Event {
    private final EventType type;
    private Packet<?> packet;

    public EventType getType() {
        return this.type;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public EventGlobalPacket(EventType type, Packet<?> packet) {
        this.type = type;
        this.packet = packet;
    }
}
