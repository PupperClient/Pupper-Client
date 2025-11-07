package com.soarclient.management.irc.client.packet.implemention.serverbound;

import com.soarclient.management.irc.client.packet.IRCPacket;
import com.soarclient.management.irc.client.packet.annotations.ProtocolField;

public class ServerBoundMessagePacket implements IRCPacket {
    @ProtocolField("m")
    private final String message;

    public ServerBoundMessagePacket(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
