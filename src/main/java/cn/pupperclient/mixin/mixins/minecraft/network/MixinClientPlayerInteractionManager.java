package cn.pupperclient.mixin.mixins.minecraft.network;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.EventPositionItem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Redirect(
        method = "sendSequencedPacket",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
        )
    )
    public void onSendSequencedPacket(ClientPlayNetworkHandler instance, Packet<?> packet) {
        if (packet instanceof PlayerInteractItemC2SPacket) {
            EventPositionItem event = new EventPositionItem(packet);
            EventBus.getInstance().post(event);

            if (!event.isCancelled()) {
                instance.sendPacket(event.getPacket());
            }
        } else {
            instance.sendPacket(packet);
        }
    }
}
