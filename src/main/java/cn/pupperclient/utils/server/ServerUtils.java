package cn.pupperclient.utils.server;

import net.minecraft.client.MinecraftClient;

import java.util.Objects;

public class ServerUtils {

	private static final MinecraftClient client = MinecraftClient.getInstance();

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
		return isMultiplayer() ? Objects.requireNonNull(client.getCurrentServerEntry()).address : "null";
	}
}
