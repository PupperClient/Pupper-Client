package com.soarclient.mixin.mixins.minecraft.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderGameOverlayEvent;
import com.soarclient.management.mod.impl.player.OldAnimationsMod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(InGameHud.class)
public class MixinInGameHud {

	/**
	 * @author EldoDebug
	 * @reason drawHeart
	 */
	@Overwrite
	private void drawHeart(DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half) {
		
    	OldAnimationsMod mod = OldAnimationsMod.getInstance();
    	
		context.drawGuiTexture(RenderLayer::getGuiTextured, type.getTexture(hardcore, half, mod.isEnabled() && mod.isDisableHeartFlash() ? false : blinking), x, y, 9, 9);
	}
    
	@Inject(method = "renderMainHud", at = @At("TAIL"))
	private void renderMainHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		EventBus.getInstance().post(new RenderGameOverlayEvent(context));
	}
}
