package cn.pupperclient.mixin.mixins.minecraft.client.render;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderGameOverlayEvent;
import cn.pupperclient.management.mod.impl.player.OldAnimationsMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

@Mixin(Gui.class)
public class MixinInGameHud {
    /**
	 * @author EldoDebug
	 * @reason drawHeart
	 */
	@Overwrite
	private void renderHeart(GuiGraphics context, Gui.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half) {
		
    	OldAnimationsMod mod = OldAnimationsMod.getInstance();
    	
		context.blitSprite(RenderType::guiTextured, type.getSprite(hardcore, half, (!mod.isEnabled() || !mod.isDisableHeartFlash()) && blinking), x, y, 9, 9);
	}
    
	@Inject(method = "renderHotbarAndDecorations", at = @At("TAIL"))
	private void renderMainHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
		EventBus.getInstance().post(new RenderGameOverlayEvent(context));
	}
}
