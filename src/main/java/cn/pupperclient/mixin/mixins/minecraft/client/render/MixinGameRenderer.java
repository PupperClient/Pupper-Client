package cn.pupperclient.mixin.mixins.minecraft.client.render;

import cn.pupperclient.management.mod.impl.render.NoHurtFov;
import cn.pupperclient.shader.impl.Kawaseblur;
import cn.pupperclient.utils.render.RenderUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private final Matrix4fStack matrices = new Matrix4fStack();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.BEFORE))
	public void render(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {

		if (HUDModSettings.getInstance().getBlurSetting().isEnabled()) {
            Kawaseblur.INGAME_BLUR.draw(minecraft., (int) HUDModSettings.getInstance().getBlurIntensitySetting().getValue());
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
            Kawaseblur.GUI_BLUR.draw(, (int) ModMenuSettings.getInstance().getBlurIntensitySetting().getValue());
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

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V"))
    private void onRenderLevel(DeltaTracker deltaTracker, CallbackInfo ci, @Local(ordinal = 0) Matrix4f projection, @Local(ordinal = 0) Matrix4f position, @Local(ordinal = 0) float tickDelta, @Local PoseStack matrixStack) {
        Matrix4f currentMatrix = new Matrix4f(matrixStack);
        Matrix4f invertedMatrix = currentMatrix.invert();
        Matrix4f correctedPosition = MixinPlugin.isIrisPresent && RenderUtils.isShaderPackInUse() ? new Matrix4f(position).mul(invertedMatrix) : new Matrix4f(position);
        RenderUtils.updateScreenCenter(projection, correctedPosition);
    }
}
