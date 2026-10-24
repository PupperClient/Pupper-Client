/*
 * Copyright (C) 2024-2026 Pupper Client contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.pupperclient.management.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import cn.pupperclient.management.websocket.client.PupperClientWebSocketClient;
import cn.pupperclient.management.websocket.packet.PupperClientPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps one websocket connection to the configured Pupper service alive.
 *
 * <p>The endpoint has no built-in default: real-time features stay off unless
 * {@code pupper.websocket.uri} (or {@code PUPPER_WEBSOCKET_URI}) names a
 * {@code ws://} or {@code wss://} service. Nothing is sent to Mojang.
 */
public class WebSocketManager implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketManager.class);

    private static final String ENDPOINT_PROPERTY = "pupper.websocket.uri";
    private static final String ENDPOINT_ENV = "PUPPER_WEBSOCKET_URI";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int CONNECTION_CHECK_INTERVAL_SECONDS = 10;

    private final Minecraft minecraftClient;
    private final ScheduledExecutorService scheduler;
    private final URI endpoint;

    private GameProfile currentGameProfile;
    private PupperClientWebSocketClient webSocketClient;
    private int retryCount = 0;
    private volatile boolean isShuttingDown;

    public WebSocketManager() {
        this.minecraftClient = Minecraft.getInstance();
        this.endpoint = resolveEndpoint();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "WebSocket-Manager");
            thread.setDaemon(true);
            return thread;
        });

        if (endpoint == null) {
            LOGGER.info("No websocket endpoint configured; real-time features are disabled");
            return;
        }
        startConnectionMonitor();
    }

    /**
     * Reads the service address from the JVM property, then the environment.
     *
     * @return the endpoint, or {@code null} when none is configured or it is not a websocket URI
     */
    private static URI resolveEndpoint() {
        String configured = System.getProperty(ENDPOINT_PROPERTY);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(ENDPOINT_ENV);
        }
        if (configured == null || configured.isBlank()) {
            return null;
        }

        try {
            URI uri = new URI(configured.strip());
            String scheme = uri.getScheme();
            if (!"ws".equals(scheme) && !"wss".equals(scheme)) {
                LOGGER.error("Ignoring websocket endpoint with unsupported scheme: {}", configured);
                return null;
            }
            return uri;
        } catch (URISyntaxException e) {
            LOGGER.error("Ignoring malformed websocket endpoint: {}", configured, e);
            return null;
        }
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
                currentGameProfile.name(), newProfile.name());
            retryCount = 0;
            return true;
        }

        return webSocketClient == null || webSocketClient.isClosed();
    }

    private void attemptConnection(GameProfile profile) {
        closeExistingConnection();
        currentGameProfile = profile;

        try {
            webSocketClient = createWebSocketClient();
            webSocketClient.connect();
            retryCount = 0;
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid WebSocket URI", e);
            retryCount++;
        } catch (Exception e) {
            LOGGER.error("Failed to establish WebSocket connection", e);
            retryCount++;
        }
    }

    private void closeExistingConnection() {
        if (webSocketClient == null) {
            return;
        }
        try {
            webSocketClient.close();
        } catch (Exception e) {
            LOGGER.warn("Error closing WebSocket connection", e);
        }
        webSocketClient = null;
    }

    private PupperClientWebSocketClient createWebSocketClient() throws URISyntaxException {
        Map<String, String> headers = new HashMap<>();
        headers.put("name", currentGameProfile.name());
        headers.put("uuid", currentGameProfile.id().toString().replace("-", ""));

        return new PupperClientWebSocketClient(endpoint, headers, this::onConnectionFailure);
    }

    private void onConnectionFailure() {
        retryCount++;
    }

    public void send(PupperClientPacket packet) {
        if (packet == null) {
            LOGGER.warn("Attempted to send null packet");
            return;
        }

        if (webSocketClient == null || !webSocketClient.isOpen()) {
            LOGGER.debug("WebSocket is not connected, cannot send packet");
            return;
        }

        try {
            String jsonData = packet.toJson().toString();
            webSocketClient.send(jsonData);
        } catch (Exception e) {
            LOGGER.error("Failed to send packet via WebSocket", e);
        }
    }

    @Override
    public void close() {
        isShuttingDown = true;
        scheduler.shutdownNow();
        closeExistingConnection();
    }
}
