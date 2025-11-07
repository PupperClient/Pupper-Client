package com.soarclient.mixin.mixins.minecraft.client.render;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.soarclient.Soar;
import com.soarclient.management.mod.impl.misc.HypixelMod;
import com.soarclient.utils.server.Server;
import com.soarclient.utils.server.ServerUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {

	@Shadow
	@Final
	private TextRenderer textRenderer;

	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", ordinal = 1))
	private void onRenderLevelHead(S inState, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			int light, CallbackInfo ci) {

		MinecraftClient client = MinecraftClient.getInstance();

		if (inState instanceof PlayerEntityRenderState state) {
			if (ServerUtils.isJoin(Server.HYPIXEL)) {

				AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) client.world.getEntityById(state.id);

				if (player != null && text.getString().contains(player.getName().getString())) {

					if (HypixelMod.getInstance().isEnabled()
							&& HypixelMod.getInstance().getLevelHeadSetting().isEnabled()) {
						String levelText = Formatting.AQUA.toString() + "Level: " + Formatting.YELLOW.toString()
								+ Soar.getInstance().getHypixelManager()
										.getByUuid(player.getUuid().toString().replace("-", "")).getNetworkLevel();

						float x = -textRenderer.getWidth(levelText) / 2F;
						float y = text.getString().contains("deadmau5") ? -20 : -10;
						int color = (int) (client.options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;

						Matrix4f matrix4f = matrices.peek().getPositionMatrix();

						textRenderer.draw(levelText, x, y, Colors.WHITE, false, matrix4f, vertexConsumers,
								TextRenderer.TextLayerType.NORMAL, color, light);
					}
				}
			}
		}
	}
}
