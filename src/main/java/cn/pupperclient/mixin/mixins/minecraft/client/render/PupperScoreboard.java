package cn.pupperclient.mixin.mixins.minecraft.client.render;

import cn.pupperclient.management.mod.impl.hud.Scoreboard;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class PupperScoreboard {
    @Inject(method = "renderScoreboardSidebar*", at = @At("HEAD"), cancellable = true)
    private void renderCustomScoreboard(CallbackInfo ci) {
        if (Scoreboard.getInstance().isEnabled()) {
            ci.cancel();
        }
    }
}
