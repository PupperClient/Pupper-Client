package cn.pupperclient.management.websocket;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import cn.pupperclient.management.websocket.client.SoarWebSocketClient;
import cn.pupperclient.management.websocket.packet.SoarPacket;
import cn.pupperclient.utils.HttpUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketManager.class);

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int CONNECTION_CHECK_INTERVAL_SECONDS = 10;

    private final MinecraftClient minecraftClient;
    private final ScheduledExecutorService scheduler;

    private GameProfile currentGameProfile;
    private SoarWebSocketClient webSocketClient;
    private int retryCount = 0;
    private final boolean isShuttingDown = false;

    public WebSocketManager() {
        this.minecraftClient = MinecraftClient.getInstance();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "WebSocket-Manager");
            thread.setDaemon(true);
            return thread;
        });

        startConnectionMonitor();
    }

    private void startConnectionMonitor() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (isShuttingDown) {
                return;
            }

            try {
                checkAndMaintainConnection();
            } catch (Exception e) {
                LOGGER.error("Error in WebSocket connection monitor", e);
            }
        }, 0, CONNECTION_CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void checkAndMaintainConnection() {
        GameProfile profile = minecraftClient.getGameProfile();

        if (profile == null) {
            LOGGER.debug("Game profile is null, skipping connection check");
            return;
        }

        boolean shouldReconnect = shouldReconnect(profile);

        if (shouldReconnect && retryCount < MAX_RETRY_ATTEMPTS) {
            attemptConnection(profile);
        } else if (retryCount >= MAX_RETRY_ATTEMPTS) {
            LOGGER.warn("Max retry attempts ({}) reached, stopping connection attempts", MAX_RETRY_ATTEMPTS);
        }
    }


    private boolean shouldReconnect(GameProfile newProfile) {
        if (currentGameProfile == null) {
            return true;
        }

        if (!currentGameProfile.equals(newProfile)) {
            LOGGER.info("User changed from {} to {}, reconnecting",
                currentGameProfile.getName(), newProfile.getName());
            retryCount = 0;
            return true;
        }

        if (webSocketClient == null || webSocketClient.isClosed()) {
            LOGGER.debug("WebSocket is closed, reconnecting");
            return true;
        }

        return false;
    }


    private void attemptConnection(GameProfile profile) {
        closeExistingConnection();
        currentGameProfile = profile;

        if (!authenticateWithMojang()) {
            LOGGER.error("Mojang authentication failed");
            retryCount++;
            return;
        }

        try {
            webSocketClient = createWebSocketClient();
            webSocketClient.connect();
            retryCount = 0; // 连接成功，重置重试计数
            //LOGGER.info("WebSocket connection established for user: {}", profile.getName());
        } catch (URISyntaxException e) {
            //LOGGER.error("Invalid WebSocket URI", e);
            retryCount++;
        } catch (Exception e) {
            //LOGGER.error("Failed to establish WebSocket connection", e);
            retryCount++;
        }
    }


    private void closeExistingConnection() {
        if (webSocketClient != null) {
            try {
                webSocketClient.close();
                //LOGGER.debug("Existing WebSocket connection closed");
            } catch (Exception e) {
                //LOGGER.warn("Error closing WebSocket connection", e);
            }
            webSocketClient = null;
        }
    }


    private boolean authenticateWithMojang() {
        Session session = minecraftClient.getSession();
        if (session == null || session.getAccessToken() == null || session.getUuidOrNull() == null) {
            LOGGER.error("Invalid Minecraft session");
            return false;
        }

        JsonObject authRequest = new JsonObject();
        authRequest.addProperty("accessToken", session.getAccessToken());
        authRequest.addProperty("selectedProfile", session.getUuidOrNull().toString().replace("-", ""));
        authRequest.addProperty("serverId", "cbd2c3f65d7ba5cceba0cc9647ff9a85c371f4");

        try {
            // 假设HttpUtils.postJson返回boolean或我们可以检查响应
            HttpUtils.postJson("https://sessionserver.mojang.com/session/minecraft/join", authRequest);
            return true;
        } catch (Exception e) {
            LOGGER.error("Mojang authentication failed", e);
            return false;
        }
    }


    private SoarWebSocketClient createWebSocketClient() throws URISyntaxException {
        Map<String, String> headers = new HashMap<>();
        headers.put("name", currentGameProfile.getName());
        headers.put("uuid", currentGameProfile.getId().toString().replace("-", ""));

        return new SoarWebSocketClient(headers, this::onConnectionFailure);
    }

    private void onConnectionFailure() {
        retryCount++;
        //LOGGER.warn("WebSocket connection failure, retry count: {}", retryCount);
    }

    public void send(SoarPacket packet) {
        if (packet == null) {
            LOGGER.warn("Attempted to send null packet");
            return;
        }

        if (webSocketClient == null || !webSocketClient.isOpen()) {
            LOGGER.warn("WebSocket is not connected, cannot send packet");
            return;
        }

        try {
            String jsonData = packet.toJson().toString();
            webSocketClient.send(jsonData);
            LOGGER.debug("Packet sent successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to send packet via WebSocket", e);
        }
    }
}
