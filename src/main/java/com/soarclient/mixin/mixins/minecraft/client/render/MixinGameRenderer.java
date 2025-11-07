package com.soarclient.mixin.mixins.minecraft.client.render;

import com.soarclient.management.mod.impl.render.NoHurtFov;
import com.soarclient.shader.impl.MipmapKawaseBlur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.management.mod.impl.player.ZoomMod;
import com.soarclient.management.mod.impl.settings.HUDModSettings;
import com.soarclient.management.mod.impl.settings.ModMenuSettings;
import com.soarclient.skia.Skia;
import com.soarclient.skia.context.SkiaContext;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", shift = At.Shift.BEFORE))
	public void render(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {

		if (HUDModSettings.getInstance().getBlurSetting().isEnabled()) {
			MipmapKawaseBlur.INGAME_BLUR.draw((int) HUDModSettings.getInstance().getBlurIntensitySetting().getValue());
		}

		SkiaContext.draw((context) -> {
			Skia.save();
			Skia.scale((float) MinecraftClient.getInstance().getWindow().getScaleFactor());
			EventBus.getInstance().post(new RenderSkiaEvent(context));
			Skia.restore();
		});
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", shift = At.Shift.AFTER))
	public void renderGuiBlur(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {

		if (HUDModSettings.getInstance().getBlurSetting().isEnabled()) {
			MipmapKawaseBlur.GUI_BLUR.draw((int) ModMenuSettings.getInstance().getBlurIntensitySetting().getValue());
		}
	}

	@Inject(method = "getFov", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
	private void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (ZoomMod.getInstance().isEnabled()) {
			float value = cir.getReturnValue();
			value = ZoomMod.getInstance().getFov(value);
			cir.setReturnValue(value);
		}
	}

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurt(CallbackInfo ci) {
        if (NoHurtFov.getInstance().isEnabled() && NoHurtFov.getInstance().nohurtFov.isEnabled()) {
            ci.cancel();
        }
    }

}
