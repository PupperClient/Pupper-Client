package cn.pupperclient.mixin.mixins.minecraft.client.render;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.management.mod.impl.render.OverlayEditorMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;

@Mixin(ScreenEffectRenderer.class)
public class MixinInGameOverlayRenderer {

	@Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
	private static void renderUnderwaterOverlay(Minecraft client, PoseStack matrices,
			MultiBufferSource vertexConsumers, CallbackInfo ci) {

		if (OverlayEditorMod.getInstance().isEnabled() && OverlayEditorMod.getInstance().isClearWater()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
	private static void renderFireOverlay(PoseStack poseStack, MultiBufferSource bufferSource, TextureAtlasSprite sprite, CallbackInfo ci) {

		if (OverlayEditorMod.getInstance().isEnabled() && OverlayEditorMod.getInstance().isClearFire()) {
			ci.cancel();
		}
	}
}
