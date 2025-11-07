package com.soarclient.management.mod.impl.misc;

import com.soarclient.Soar;
import com.soarclient.event.EventBus;
import com.soarclient.event.client.ClientTickEvent;
import com.soarclient.management.irc.client.IRCTransport;
import com.soarclient.management.irc.client.IRCHandler;
import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.management.mod.settings.impl.BooleanSetting;
import com.soarclient.management.mod.settings.impl.NumberSetting;
import com.soarclient.management.mod.settings.impl.StringSetting;
import com.soarclient.skia.font.Icon;
import com.soarclient.utils.IMinecraft;
import net.minecraft.text.Text;

import java.io.IOException;

public class IRCChatMod extends Mod implements IMinecraft, IRCHandler {

    // Settings
    private final StringSetting serverSetting = new StringSetting("setting.irc.server",
        "setting.irc.server.description", Icon.SERVICE_TOOLBOX, this, "localhost");

    private final NumberSetting portSetting = new NumberSetting("setting.irc.port",
        "setting.irc.port.description", Icon.SERVICE_TOOLBOX, this, 8888, 1, 65535, 1);

    private final StringSetting usernameSetting = new StringSetting("setting.irc.username",
        "setting.irc.username.description", Icon.PERSON_2, this, "SoarUser");

    private final StringSetting passwordSetting = new StringSetting("setting.irc.password",
        "setting.irc.password.description", Icon.PASSWORD, this, "123");

    private final BooleanSetting autoConnectSetting = new BooleanSetting("setting.irc.auto_connect",
        "setting.irc.auto_connect.description", Icon.CHECK, this, false);

    private final BooleanSetting showMessagesSetting = new BooleanSetting("setting.irc.show_messages",
        "setting.irc.show_messages.description", Icon.CHAT, this, true);

    // IRC Components
    private IRCTransport transport;
    private boolean isConnected = false;
    private String lastError = "";
    private long lastReconnectAttempt = 0;
    private static final long RECONNECT_DELAY = 5000; // 5 seconds
    private static IRCChatMod instance;

    public IRCChatMod() {
        super("mod.irc.name", "mod.irc.description", Icon.CHAT, ModCategory.MISC);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        EventBus.getInstance().register(this);

        if (autoConnectSetting.isEnabled()) {
            connectToIRC();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        EventBus.getInstance().unregister(this);
        disconnectFromIRC();
    }

    // IRCHandler implementation
    @Override
    public void onMessage(String sender, String message) throws IOException {
        if (showMessagesSetting.isEnabled() && mc.player != null) {
            String formattedMessage = String.format("§9[IRC] §b%s§f: %s", sender, message);
            mc.player.sendMessage(Text.of(formattedMessage), false);
        }
    }

    @Override
    public void onDisconnected(String message) {
        isConnected = false;
        lastError = message;
        Soar.LOGGER.warn("IRC disconnected: {}", message);

        if (mc.player != null) {
            mc.player.sendMessage(Text.of("§cIRC disconnected: " + message), false);
        }

        // Schedule reconnect if auto-connect is enabled
        if (autoConnectSetting.isEnabled()) {
            lastReconnectAttempt = System.currentTimeMillis();
        }
    }

    @Override
    public void onConnected() {
        isConnected = true;
        lastError = "";
        Soar.LOGGER.info("IRC connected successfully");

        if (mc.player != null) {
            mc.player.sendMessage(Text.of("§aConnected to IRC server!"), false);
        }
    }

    @Override
    public String getInGameUsername() {
        // Use Minecraft username if available, otherwise use setting
        if (mc.player != null) {
            return mc.player.getGameProfile().getName();
        }
        return usernameSetting.getValue();
    }

    // Tick event for maintaining connection
    public EventBus.EventListener<ClientTickEvent> onClientTick = event -> {
        // Handle automatic reconnection
        if (autoConnectSetting.isEnabled() && !isConnected && transport == null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastReconnectAttempt > RECONNECT_DELAY) {
                Soar.LOGGER.info("Attempting to reconnect to IRC...");
                connectToIRC();
                lastReconnectAttempt = currentTime;
            }
        }

        // Update in-game name periodically if connected
        if (isConnected && transport != null && mc.player != null) {
            // This will be handled by the transport's scheduled task
        }
    };

    // Public methods for command access
    public void connectToIRC() {
        if (isConnected) {
            sendChatMessage("§cAlready connected to IRC");
            return;
        }

        new Thread(() -> {
            try {
                Soar.LOGGER.info("Connecting to IRC server {}:{}", serverSetting.getValue(), portSetting.getValue());

                transport = new IRCTransport(
                    serverSetting.getValue(),
                    (int) portSetting.getValue(),
                    this
                );

                // Connect with credentials
                transport.connect(usernameSetting.getValue(), passwordSetting.getValue());

            } catch (IOException e) {
                lastError = e.getMessage();
                Soar.LOGGER.error("Failed to create IRC transport", e);

                if (mc.player != null) {
                    mc.player.sendMessage(Text.of("§cFailed to connect to IRC: " + e.getMessage()), false);
                }

                transport = null;
                isConnected = false;
            }
        }, "IRC-Connector").start();
    }

    public void disconnectFromIRC() {
        if (transport != null) {
            try {
                transport.disconnect();
            } catch (Exception e) {
                Soar.LOGGER.error("Error disconnecting from IRC", e);
            }
            transport = null;
        }
        isConnected = false;
        sendChatMessage("§6Disconnected from IRC");
    }

    public void sendIRCMessage(String message) {
        if (!isConnected || transport == null) {
            sendChatMessage("§cNot connected to IRC. Use .irc connect first.");
            return;
        }

        try {
            transport.sendChat(message);
        } catch (Exception e) {
            sendChatMessage("§cFailed to send IRC message: " + e.getMessage());
            Soar.LOGGER.error("Failed to send IRC message", e);
        }
    }

    // Utility methods
    public boolean isConnected() {
        return isConnected && transport != null;
    }

    public String getConnectionStatus() {
        if (isConnected()) {
            return "§aConnected";
        } else if (!lastError.isEmpty()) {
            return "§cError: " + lastError;
        } else {
            return "§6Disconnected";
        }
    }

    public String getServerInfo() {
        return serverSetting.getValue() + ":" + portSetting.getValue();
    }

    private void sendChatMessage(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.of(message), false);
        }
    }

    // Cleanup
    @Override
    public void cleanup() {
        super.cleanup();
        disconnectFromIRC();
    }

    public static IRCChatMod getInstance() {
        return instance;
    }
}
