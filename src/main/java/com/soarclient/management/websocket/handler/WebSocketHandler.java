package com.soarclient.management.websocket.handler;

import com.google.gson.JsonObject;

public abstract class WebSocketHandler {
	public abstract void handle(JsonObject jsonObject);
}
