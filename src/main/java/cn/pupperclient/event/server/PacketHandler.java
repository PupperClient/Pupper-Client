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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

public class PacketHandler {

	public final EventBus.EventListener<SendPacketEvent> onSendPacket = packetEvent -> {

		Packet<?> basePacket = packetEvent.getPacket();

		if (basePacket instanceof ServerboundInteractPacket) {

			ServerboundInteractPacket packet = (ServerboundInteractPacket) basePacket;
			ServerboundInteractPacket.ActionType type = ((PlayerInteractEntityC2SPacketAccessor) packet)
					.getInteractTypeHandler().getType();

			if (type.equals(ServerboundInteractPacket.ActionType.ATTACK)) {
				EventBus.getInstance()
						.post(new AttackEntityEvent(((PlayerInteractEntityC2SPacketAccessor) packet).entityId()));
			}
		}

		if (basePacket instanceof ServerboundChatPacket) {

			ServerboundChatPacket packet = (ServerboundChatPacket) basePacket;
			SendChatEvent event = new SendChatEvent(packet.message());

			EventBus.getInstance().post(event);

			if (event.isCancelled()) {
				packetEvent.setCancelled(true);
			}
		}
	};

	public final EventBus.EventListener<ReceivePacketEvent> onReceivePacket = packetEvent -> {

		Packet<?> basePacket = packetEvent.getPacket();

		if (basePacket instanceof ClientboundDamageEventPacket) {

			ClientboundDamageEventPacket packet = (ClientboundDamageEventPacket) basePacket;

			EventBus.getInstance().post(new DamageEntityEvent(packet.entityId()));
		}

		if (basePacket instanceof ClientboundPlayerChatPacket) {

			ClientboundPlayerChatPacket packet = (ClientboundPlayerChatPacket) basePacket;
			ReceiveChatEvent event = new ReceiveChatEvent(packet.body().content());

			EventBus.getInstance().post(event);

			if (event.isCancelled()) {
				packetEvent.setCancelled(true);
			}
		}

		if (basePacket instanceof ClientboundSystemChatPacket) {

			ClientboundSystemChatPacket packet = (ClientboundSystemChatPacket) basePacket;
			ReceiveChatEvent event = new ReceiveChatEvent(packet.content().getString());

			EventBus.getInstance().post(event);

			if (event.isCancelled()) {
				packetEvent.setCancelled(true);
			}
		}

		if (basePacket instanceof ClientboundLoginPacket) {
			EventBus.getInstance().post(new GameJoinEvent());
		}
	};
}
