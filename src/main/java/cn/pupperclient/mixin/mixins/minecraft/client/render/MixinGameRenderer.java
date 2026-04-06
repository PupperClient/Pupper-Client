package cn.pupperclient.mixin.mixins.minecraft.client.render;

import cn.pupperclient.management.mod.impl.render.NoHurtFov;
import cn.pupperclient.shader.impl.Kawaseblur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.impl.player.ZoomMod;
import cn.pupperclient.management.mod.impl.settings.HUDModSettings;
import cn.pupperclient.management.mod.impl.settings.ModMenuSettings;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.context.SkiaContext;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.BEFORE))
	public void render(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {

		if (HUDModSettings.getInstance().getBlurSetting().isEnabled()) {
            Kawaseblur.INGAME_BLUR.draw((int) HUDModSettings.getInstance().getBlurIntensitySetting().getValue());
		}

		SkiaContext.draw((context) -> {
			Skia.save();
			Skia.scale((float) Minecraft.getInstance().getWindow().getGuiScale());
			EventBus.getInstance().post(new RenderSkiaEvent(context));
			Skia.restore();
		});
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER))
	public void renderGuiBlur(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {

		if (HUDModSettings.getInstance().getBlurSetting().isEnabled()) {
            Kawaseblur.GUI_BLUR.draw((int) ModMenuSettings.getInstance().getBlurIntensitySetting().getValue());
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

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurt(CallbackInfo ci) {
        if (NoHurtFov.getInstance().isEnabled() && NoHurtFov.getInstance().nohurtFov.isEnabled()) {
            ci.cancel();
        }
    }

}
