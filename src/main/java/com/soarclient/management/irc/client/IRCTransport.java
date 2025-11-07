package com.soarclient.management.irc.client;

import com.soarclient.management.irc.client.packet.IRCPacket;
import com.soarclient.management.irc.client.packet.implemention.clientbound.*;
import com.soarclient.management.irc.client.packet.implemention.serverbound.*;
import com.soarclient.management.irc.client.processor.IRCProtocol;
import com.soarclient.utils.ChatUtils;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IRCTransport {
    private final IRCProtocol protocol = new IRCProtocol();
    private final AioSession session;
    private IRCHandler handler;
    private final Map<String,String> userToIgnMap = new ConcurrentHashMap<>();
    private final Map<String,String> ignToUserMap = new ConcurrentHashMap<>();

    public IRCTransport(String host, int port,IRCHandler handler) throws IOException {
        this.handler = handler;
        MessageProcessor<IRCPacket> processor = (session, msg) -> {
            if(msg instanceof ClientBoundDisconnectPacket){
                handler.onDisconnected(((ClientBoundDisconnectPacket) msg).reason());
            }
            if(msg instanceof ClientBoundConnectedPacket){
                handler.onConnected();
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                Runnable task = this::sendInGameUsername;
                scheduler.scheduleAtFixedRate(task, 5, 5, TimeUnit.SECONDS);
            }
            if(msg instanceof ClientBoundUpdateUserListPacket){
                userToIgnMap.clear();
                userToIgnMap.putAll(((ClientBoundUpdateUserListPacket) msg).getUserMap());
                ignToUserMap.clear();
                userToIgnMap.forEach((user, ign) -> ignToUserMap.put(ign,user));
            }
            if(msg instanceof ClientBoundMessagePacket){
                try {
                    handler.onMessage(((ClientBoundMessagePacket) msg).sender(),((ClientBoundMessagePacket) msg).message());
                } catch (IOException e) {
                    ChatUtils.addChatMessage("IRC Error: " + e.getMessage());
                }
            }
        };
        AioQuickClient client = new AioQuickClient(host, port, protocol, processor);
        session = client.start();
    }

    public void sendPacket(IRCPacket packet){
        try {
            byte[] data = protocol.encode(packet);
            session.writeBuffer().writeInt(data.length);
            session.writeBuffer().write(data);
            session.writeBuffer().flush();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public boolean isUser(String name){
        return ignToUserMap.containsKey(name);
    }

    public String getName(String ign){
        return ignToUserMap.get(ign);
    }

    public String getIgn(String name){
        return userToIgnMap.get(name);
    }

    public void setIgn(String name, String ign){
        userToIgnMap.put(name, ign);
        ignToUserMap.put(ign, name);
    }

    public void sendChat(String message){
        sendPacket(new ServerBoundMessagePacket(message));
    }

    public void sendInGameUsername(String username){
        sendPacket(new ServerBoundUpdateIgnPacket(username));
    }

    public void sendInGameUsername(){
        sendInGameUsername(handler.getInGameUsername());
    }

    public void connect(String username,String token){
        sendPacket(new ServerBoundHandshakePacket(username,token));
    }

    public void setHandler(IRCHandler handler) {
        this.handler = handler;
    }

    public void disconnect() {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                // Ignore errors during close
            }
        }
    }

    // 同时添加连接状态检查方法
    public boolean isConnected() {
        return session != null && session.isInvalid();
    }
}
