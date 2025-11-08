package cn.pupperclient.management.irc.client.packet.implemention.serverbound;

import cn.pupperclient.management.irc.client.packet.IRCPacket;
import cn.pupperclient.management.irc.client.packet.annotations.ProtocolField;

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
