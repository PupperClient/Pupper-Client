package com.soarclient.management.irc.client.packet.implemention.clientbound;

import com.soarclient.management.irc.client.packet.IRCPacket;
import com.soarclient.management.irc.client.packet.annotations.ProtocolField;

public class ClientBoundMessagePacket implements IRCPacket {
    @ProtocolField("s")
    private final String sender;
    @ProtocolField("m")
    private final String message;

    public ClientBoundMessagePacket(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String sender() {
        return sender;
    }

    public String message() {
        return message;
    }
}
