package cn.pupperclient.management.irc.client.packet.implemention.clientbound;

import cn.pupperclient.management.irc.client.packet.IRCPacket;
import cn.pupperclient.management.irc.client.packet.annotations.ProtocolField;

public class ClientBoundDisconnectPacket implements IRCPacket {
    @ProtocolField("r")
    private final String reason;

    public ClientBoundDisconnectPacket(String reason) {
        this.reason = reason;
    }

    public String reason() {
        return reason;
    }
}
