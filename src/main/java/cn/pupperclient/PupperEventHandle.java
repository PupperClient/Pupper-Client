package cn.pupperclient;

import java.util.List;
import java.util.Objects;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.EventType;
import cn.pupperclient.event.client.*;
import cn.pupperclient.event.server.impl.GameJoinEvent;
import cn.pupperclient.management.profile.Profile;
import cn.pupperclient.utils.ChatUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

public class PupperEventHandle {

    @EventListener
    public void onMotion(EventMotion e) {
        if (MinecraftClient.getInstance().player != null && e.getType() == EventType.PRE && MinecraftClient.getInstance().player.deathTime <= 1) {
            EventBus.getInstance().post(new EventRespawn());
        }
    }

    @EventListener
    public void onUseTotem(EventUseTotem e){
        ChatUtils.addChatMessage("[i] " + Objects.requireNonNull(e.source.getSource()).getName() + " used a totem!");
    }

	public final EventBus.EventListener<ClientTickEvent> onClientTick = event -> {
		PupperClient.getInstance().getColorManager().onTick();
		PupperClient.getInstance().getHypixelManager().update();
		PupperClient.getInstance().getUserManager().update();
	};

	public final EventBus.EventListener<GameJoinEvent> onGameJoin = event -> {
		PupperClient.getInstance().getHypixelManager().clear();
		PupperClient.getInstance().getUserManager().clear();
	};

	public final EventBus.EventListener<ServerJoinEvent> onServerJoin = event -> {

		List<Profile> profiles = PupperClient.getInstance().getProfileManager().getProfiles();

		for (Profile p : profiles) {

			String address = p.getServerIp();

			if (event.getAddress().contains(address)) {
				PupperClient.getInstance().getProfileManager().load(p);
				break;
			}
		}
	};
}
