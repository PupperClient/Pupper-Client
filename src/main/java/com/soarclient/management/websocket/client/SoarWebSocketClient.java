package com.soarclient.management.websocket.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.soarclient.logger.SoarLogger;
import com.soarclient.management.websocket.handler.WebSocketHandler;
import com.soarclient.management.websocket.handler.impl.HypixelStatsHandler;
import com.soarclient.management.websocket.handler.impl.SoarUserHandler;
import com.soarclient.utils.JsonUtils;

public class SoarWebSocketClient extends WebSocketClient {

	private final Map<String, WebSocketHandler> handlers = new HashMap<>();
	private final Gson gson = new Gson();
	private final Runnable closeTask;

	public SoarWebSocketClient(Map<String, String> headers, Runnable closeTask) throws URISyntaxException {
		super(new URI("ws://localhost:8080/websocket"), headers);
		this.closeTask = closeTask;
		initializeHandlers();
	}

    private void initializeHandlers() {
    	register("sc-hypixel-stats", new HypixelStatsHandler());
    	register("sc-soar-user", new SoarUserHandler());
    }
    
	@Override
	public void onOpen(ServerHandshake handshakedata) {
		SoarLogger.info("API", "WebSocket connection opened");
	}

	@Override
	public void onMessage(String message) {

		JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
		
		String type = JsonUtils.getStringProperty(jsonObject, "type", "");

		WebSocketHandler handler = handlers.get(type);

		if (handler != null) {
			handler.handle(jsonObject);
		} else {
			//SoarLogger.warn("API", "No handler found for message type: " + type);
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		//SoarLogger.info("API", "WebSocket connection closed: " + reason);
		closeTask.run();
	}

	@Override
	public void onError(Exception ex) {
		//SoarLogger.error("API", "WebSocket error occurred", ex);
	}

	private void register(String type, WebSocketHandler handler) {
		handlers.put(type, handler);
	}
}
