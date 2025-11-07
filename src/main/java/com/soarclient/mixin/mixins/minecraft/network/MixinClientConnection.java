package com.soarclient.mixin.mixins.minecraft.network;

import net.minecraft.network.PacketCallbacks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.ReceivePacketEvent;
import com.soarclient.event.client.SendPacketEvent;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {

	@Shadow
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
	}

    @Shadow
    protected abstract void sendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush);

	@Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
	private void onSendPacket(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {

		SendPacketEvent event = new SendPacketEvent(packet);
		EventBus.getInstance().post(event);

		if (event.isCancelled()) {
			ci.cancel();
            ClientConnection self = (ClientConnection) (Object) this;
            self.send(event.getPacket(), callbacks);
		}
	}

	@Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true, require = 1)
	private static void onRecievePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {

		if (packet instanceof BundleS2CPacket bundleS2CPacket) {
			ci.cancel();

			for (Packet<?> packetInBundle : bundleS2CPacket.getPackets()) {
				try {
					handlePacket(packetInBundle, listener);
				} catch (OffThreadException ignored) {
				}
			}
			return;
		}

		ReceivePacketEvent event = new ReceivePacketEvent(packet);
		EventBus.getInstance().post(event);

		if (event.isCancelled()) {
			ci.cancel();
		}
	}
}
