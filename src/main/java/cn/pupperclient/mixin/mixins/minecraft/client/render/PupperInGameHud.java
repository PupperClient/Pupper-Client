package cn.pupperclient.mixin.mixins.minecraft.client.render;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.DrawItemHotbarEvent;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class PupperInGameHud {
    @Shadow @Final private Minecraft minecraft;

    @Shadow
    private Player getCameraPlayer() {
        Entity var2 = this.minecraft.getCameraEntity();
        Player var10000;
        if (var2 instanceof Player playerEntity) {
            var10000 = playerEntity;
        } else {
            var10000 = null;
        }
        return var10000;
    }

    @Unique private long lastHotbarUpdate = 0;
    @Unique private int lastSelectedSlot = 0;
    @Unique private float slotAnimationProgress = 0f;
    @Unique private float offhandAlpha = 0f;

    @Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true)
    private void replaceHotbarRender(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        DrawItemHotbarEvent event = new DrawItemHotbarEvent();
        EventBus.getInstance().post(event);

        if (event.isCancelled()) ci.cancel();
    }
}
