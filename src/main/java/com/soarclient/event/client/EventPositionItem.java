package com.soarclient.event.client;

import com.soarclient.event.Event;
import net.minecraft.network.packet.Packet;

public class EventPositionItem extends Event {
    private Packet<?> packet;

    public Packet<?> getPacket() {
        return this.packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public EventPositionItem(Packet<?> packet) {
        this.packet = packet;
    }
}
