package com.soarclient.mixin.mixins.minecraft.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.soarclient.management.mod.impl.render.OverlayEditorMod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {

	@Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, CallbackInfo ci) {

		if (OverlayEditorMod.getInstance().isEnabled() && OverlayEditorMod.getInstance().isClearWater()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			CallbackInfo ci) {

		if (OverlayEditorMod.getInstance().isEnabled() && OverlayEditorMod.getInstance().isClearFire()) {
			ci.cancel();
		}
	}
}
