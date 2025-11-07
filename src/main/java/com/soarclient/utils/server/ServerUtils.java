package com.soarclient.utils.server;

import net.minecraft.client.MinecraftClient;

public class ServerUtils {

	private static MinecraftClient client = MinecraftClient.getInstance();

	public static boolean isJoin(Server server) {
		return isMultiplayer() && getAddress().contains(server.getAddress());
	}

	public static boolean isSingleplayer() {
		return client.isConnectedToLocalServer();
	}

	public static boolean isMultiplayer() {
		return client.getCurrentServerEntry() != null;
	}

	public static String getAddress() {
		return isMultiplayer() ? client.getCurrentServerEntry().address : "null";
	}
}
