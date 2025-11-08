package cn.pupperclient.management.websocket.handler.impl;

import com.google.gson.JsonObject;
import cn.pupperclient.PupperClient;
import cn.pupperclient.management.user.UserManager;
import cn.pupperclient.management.websocket.handler.WebSocketHandler;
import cn.pupperclient.utils.JsonUtils;

public class SoarUserHandler extends WebSocketHandler {

	@Override
	public void handle(JsonObject jsonObject) {
		
		UserManager userManager = PupperClient.getInstance().getUserManager();
		
		String uuid = JsonUtils.getStringProperty(jsonObject, "uuid", "null");
		boolean isUser = JsonUtils.getBooleanProperty(jsonObject, "soarUser", false);
		
		if(!uuid.equals("null")) {
			userManager.add(uuid, isUser);
		}
	}
}
