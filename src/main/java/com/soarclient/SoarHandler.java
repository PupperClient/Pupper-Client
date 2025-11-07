package com.soarclient;

import java.util.List;

import com.soarclient.event.EventBus;
import com.soarclient.event.EventListener;
import com.soarclient.event.EventType;
import com.soarclient.event.client.ClientTickEvent;
import com.soarclient.event.client.EventMotion;
import com.soarclient.event.client.EventRespawn;
import com.soarclient.event.client.ServerJoinEvent;
import com.soarclient.event.server.impl.GameJoinEvent;
import com.soarclient.management.profile.Profile;
import net.minecraft.client.MinecraftClient;

public class SoarHandler {

    @EventListener
    public void onMotion(EventMotion e) {
        if (MinecraftClient.getInstance().player != null && e.getType() == EventType.PRE && MinecraftClient.getInstance().player.deathTime <= 1) {
            EventBus.getInstance().post(new EventRespawn());
        }
    }

	public final EventBus.EventListener<ClientTickEvent> onClientTick = event -> {
		Soar.getInstance().getColorManager().onTick();
		Soar.getInstance().getHypixelManager().update();
		Soar.getInstance().getUserManager().update();
	};

	public final EventBus.EventListener<GameJoinEvent> onGameJoin = event -> {
		Soar.getInstance().getHypixelManager().clear();
		Soar.getInstance().getUserManager().clear();
	};

	public final EventBus.EventListener<ServerJoinEvent> onServerJoin = event -> {

		List<Profile> profiles = Soar.getInstance().getProfileManager().getProfiles();

		for (Profile p : profiles) {

			String address = p.getServerIp();

			if (event.getAddress().contains(address)) {
				Soar.getInstance().getProfileManager().load(p);
				break;
			}
		}
	};
}
