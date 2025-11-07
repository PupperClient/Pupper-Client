package com.soarclient.management.irc.client.packet.implemention.clientbound;

import com.soarclient.management.irc.client.packet.IRCPacket;
import com.soarclient.management.irc.client.packet.annotations.ProtocolField;

import java.util.Map;

public class ClientBoundUpdateUserListPacket implements IRCPacket {
    @ProtocolField("u")
    private final Map<String,String> userMap;

    public ClientBoundUpdateUserListPacket(Map<String, String> userMap) {
        this.userMap = userMap;
    }

    public Map<String, String> getUserMap() {
        return userMap;
    }
}
