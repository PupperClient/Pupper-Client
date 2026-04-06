package cn.pupperclient.mixin.mixins.minecraft.network;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.ReceivePacketEvent;
import cn.pupperclient.event.client.SendPacketEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.RunningOnDifferentThreadException;

@Mixin(Connection.class)
public abstract class MixinClientConnection {

	@Shadow
    private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener listener) {
	}

    @Shadow
    protected abstract void sendPacket(Packet<?> packet, @Nullable PacketSendListener callbacks, boolean flush);

	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"), cancellable = true)
	private void onSendPacket(Packet<?> packet, PacketSendListener callbacks, CallbackInfo ci) {

		SendPacketEvent event = new SendPacketEvent(packet);
		EventBus.getInstance().post(event);

		if (event.isCancelled()) {
			ci.cancel();
            Connection self = (Connection) (Object) this;
            self.send(event.getPacket(), callbacks);
		}
	}

	@Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true, require = 1)
	private static void onRecievePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {

		if (packet instanceof ClientboundBundlePacket bundleS2CPacket) {
			ci.cancel();

			for (Packet<?> packetInBundle : bundleS2CPacket.subPackets()) {
				try {
					genericsFtw(packetInBundle, listener);
				} catch (RunningOnDifferentThreadException ignored) {
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
