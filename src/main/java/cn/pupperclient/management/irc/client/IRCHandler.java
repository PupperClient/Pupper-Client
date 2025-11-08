package cn.pupperclient.management.irc.client;

import java.io.IOException;

public interface IRCHandler {
    void onMessage(String sender,String message) throws IOException;
    void onDisconnected(String message);
    void onConnected();
    String getInGameUsername();
}
