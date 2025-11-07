package com.soarclient.management.websocket.handler.impl;

import com.google.gson.JsonObject;
import com.soarclient.Soar;
import com.soarclient.management.user.UserManager;
import com.soarclient.management.websocket.handler.WebSocketHandler;
import com.soarclient.utils.JsonUtils;

public class SoarUserHandler extends WebSocketHandler {

	@Override
	public void handle(JsonObject jsonObject) {
		
		UserManager userManager = Soar.getInstance().getUserManager();
		
		String uuid = JsonUtils.getStringProperty(jsonObject, "uuid", "null");
		boolean isUser = JsonUtils.getBooleanProperty(jsonObject, "soarUser", false);
		
		if(!uuid.equals("null")) {
			userManager.add(uuid, isUser);
		}
	}
}
