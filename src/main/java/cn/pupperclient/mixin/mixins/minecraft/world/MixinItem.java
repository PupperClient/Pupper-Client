package cn.pupperclient.mixin.mixins.minecraft.world;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.EventUseItemRayTrace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public class MixinItem {
    @Redirect(
        method = {"raycast"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"
        )
    )
    private static float hookRayTraceYRot(PlayerEntity instance) {
        EventUseItemRayTrace event = new EventUseItemRayTrace(instance.getYaw(), instance.getPitch());
        EventBus.getInstance().post(event);
        return event.getYaw();
    }
}
