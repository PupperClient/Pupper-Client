package cn.pupperclient.mixin.mixins.minecraft.client.render;

import cn.pupperclient.management.mod.impl.render.NoHurtFov;
import cn.pupperclient.shader.impl.Kawaseblur;
import com.mojang.blaze3d.systems.RenderSystem;
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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", shift = At.Shift.BEFORE))
	public void render(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {

        if (HUDModSettings.getInstance().getBlurSetting().isEnabled()) {
            int intensity = (int) HUDModSettings.getInstance().getBlurIntensitySetting().getValue();
            var encoder = RenderSystem.getDevice().createCommandEncoder();
            Kawaseblur.INGAME_BLUR.draw(encoder, intensity);
        }
//
//		SkiaContext.draw((context) -> {
//			Skia.save();
//			Skia.scale((float) MinecraftClient.getInstance().getWindow().getScaleFactor());
//			EventBus.getInstance().post(new RenderSkiaEvent(context));
//			Skia.restore();
//		});
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", shift = At.Shift.AFTER))
	public void renderGuiBlur(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (HUDModSettings.getInstance().getBlurSetting().isEnabled()) {
            // 使用 ModMenuSettings 或 HUDModSettings 中定义的强度
            int intensity = (int) ModMenuSettings.getInstance().getBlurIntensitySetting().getValue();
            var encoder = RenderSystem.getDevice().createCommandEncoder();
            Kawaseblur.GUI_BLUR.draw(encoder, intensity);
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
