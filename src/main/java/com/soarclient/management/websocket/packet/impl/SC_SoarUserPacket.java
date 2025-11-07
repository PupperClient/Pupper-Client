package com.soarclient.management.websocket.packet.impl;

import com.google.gson.JsonObject;
import com.soarclient.management.websocket.packet.SoarPacket;

public class SC_SoarUserPacket extends SoarPacket {

	private final String uuid;
	
	public SC_SoarUserPacket(String uuid) {
		super("sc-soar-user");
		this.uuid = uuid;
	}

	@Override
	public JsonObject toJson() {
		jsonObject.addProperty("uuid", uuid);
		return jsonObject;
	}
}
