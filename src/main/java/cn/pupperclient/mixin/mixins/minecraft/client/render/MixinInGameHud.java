package cn.pupperclient.mixin.mixins.minecraft.client.render;

import cn.pupperclient.management.mod.impl.player.OldAnimationsMod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    /**
	 * @author EldoDebug
	 * @reason drawHeart
	 */
	@Overwrite
	private void drawHeart(DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half) {
		
    	OldAnimationsMod mod = OldAnimationsMod.getInstance();
		context.drawGuiTexture(
            RenderLayer::getGuiTextured,
            type.getTexture(hardcore, half, (!mod.isEnabled() || !mod.isDisableHeartFlash()) && blinking),
            x,
            y,
            9,
            9
        );
	}
}
