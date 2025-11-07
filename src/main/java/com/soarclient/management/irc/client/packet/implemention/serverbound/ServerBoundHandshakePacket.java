package com.soarclient.management.irc.client.packet.implemention.serverbound;

import com.soarclient.management.irc.client.packet.IRCPacket;
import com.soarclient.management.irc.client.packet.annotations.ProtocolField;


public class ServerBoundHandshakePacket implements IRCPacket {
    @ProtocolField("u")
    private final String username;
    @ProtocolField("t")
    private final String token;

    public ServerBoundHandshakePacket(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String username() {
        return username;
    }

    public String token() {
        return token;
    }
}
