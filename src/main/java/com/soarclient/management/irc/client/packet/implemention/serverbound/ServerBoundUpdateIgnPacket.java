package com.soarclient.management.irc.client.packet.implemention.serverbound;

import com.soarclient.management.irc.client.packet.IRCPacket;
import com.soarclient.management.irc.client.packet.annotations.ProtocolField;

public class ServerBoundUpdateIgnPacket implements IRCPacket {
    @ProtocolField("n")
    private final String name;

    public ServerBoundUpdateIgnPacket(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
