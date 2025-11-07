package com.soarclient.event.client;

import com.soarclient.event.Event;

import net.minecraft.network.packet.Packet;

public class ReceivePacketEvent extends Event {

	private Packet<?> packet;

	public ReceivePacketEvent(Packet<?> packet) {
		this.packet = packet;
	}

	public Packet<?> getPacket() {
		return packet;
	}

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
}
