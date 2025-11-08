package cn.pupperclient.event.server;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.ReceivePacketEvent;
import cn.pupperclient.event.client.SendPacketEvent;
import cn.pupperclient.event.server.impl.AttackEntityEvent;
import cn.pupperclient.event.server.impl.DamageEntityEvent;
import cn.pupperclient.event.server.impl.GameJoinEvent;
import cn.pupperclient.event.server.impl.ReceiveChatEvent;
import cn.pupperclient.event.server.impl.SendChatEvent;
import cn.pupperclient.mixin.mixins.minecraft.network.packet.PlayerInteractEntityC2SPacketAccessor;

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
