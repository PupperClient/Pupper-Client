package com.soarclient.event.server;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.ReceivePacketEvent;
import com.soarclient.event.client.SendPacketEvent;
import com.soarclient.event.server.impl.AttackEntityEvent;
import com.soarclient.event.server.impl.DamageEntityEvent;
import com.soarclient.event.server.impl.GameJoinEvent;
import com.soarclient.event.server.impl.ReceiveChatEvent;
import com.soarclient.event.server.impl.SendChatEvent;
import com.soarclient.mixin.mixins.minecraft.network.packet.PlayerInteractEntityC2SPacketAccessor;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class PacketHandler {

	public final EventBus.EventListener<SendPacketEvent> onSendPacket = packetEvent -> {

		Packet<?> basePacket = packetEvent.getPacket();

		if (basePacket instanceof PlayerInteractEntityC2SPacket) {

			PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket) basePacket;
			PlayerInteractEntityC2SPacket.InteractType type = ((PlayerInteractEntityC2SPacketAccessor) packet)
					.getInteractTypeHandler().getType();

			if (type.equals(PlayerInteractEntityC2SPacket.InteractType.ATTACK)) {
				EventBus.getInstance()
						.post(new AttackEntityEvent(((PlayerInteractEntityC2SPacketAccessor) packet).entityId()));
			}
		}

		if (basePacket instanceof ChatMessageC2SPacket) {

			ChatMessageC2SPacket packet = (ChatMessageC2SPacket) basePacket;
			SendChatEvent event = new SendChatEvent(packet.chatMessage());

			EventBus.getInstance().post(event);

			if (event.isCancelled()) {
				packetEvent.setCancelled(true);
			}
		}
	};

	public final EventBus.EventListener<ReceivePacketEvent> onReceivePacket = packetEvent -> {

		Packet<?> basePacket = packetEvent.getPacket();

		if (basePacket instanceof EntityDamageS2CPacket) {

			EntityDamageS2CPacket packet = (EntityDamageS2CPacket) basePacket;

			EventBus.getInstance().post(new DamageEntityEvent(packet.entityId()));
		}

		if (basePacket instanceof ChatMessageS2CPacket) {

			ChatMessageS2CPacket packet = (ChatMessageS2CPacket) basePacket;
			ReceiveChatEvent event = new ReceiveChatEvent(packet.body().content());

			EventBus.getInstance().post(event);

			if (event.isCancelled()) {
				packetEvent.setCancelled(true);
			}
		}

		if (basePacket instanceof GameMessageS2CPacket) {

			GameMessageS2CPacket packet = (GameMessageS2CPacket) basePacket;
			ReceiveChatEvent event = new ReceiveChatEvent(packet.content().getString());

			EventBus.getInstance().post(event);

			if (event.isCancelled()) {
				packetEvent.setCancelled(true);
			}
		}

		if (basePacket instanceof GameJoinS2CPacket) {
			EventBus.getInstance().post(new GameJoinEvent());
		}
	};
}
