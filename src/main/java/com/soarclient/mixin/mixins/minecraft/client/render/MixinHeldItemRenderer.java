package com.soarclient.mixin.mixins.minecraft.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.soarclient.management.mod.impl.player.OldAnimationsMod;
import com.soarclient.management.mod.impl.render.CustomHandMod;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BowItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {

	@Shadow
	protected abstract void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress);

	@Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = ("Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"), ordinal = 1))
	private void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
			float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {

		OldAnimationsMod mod = OldAnimationsMod.getInstance();

		if (item.getItem() instanceof BowItem && mod.isEnabled() && mod.isOldBow()) {
			matrices.translate(0f, 0.05f, 0.04f);
			matrices.scale(0.93f, 1f, 1f);
		} else if (item.getItem() instanceof FishingRodItem && mod.isEnabled() && mod.isOldRod()) {
			matrices.translate(0.08f, -0.027f, -0.33f);
			matrices.scale(0.93f, 1f, 1f);
		}
	}
	
	@Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
	private void applyCustomHand(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
			float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		
		CustomHandMod mod = CustomHandMod.getInstance();
		
		if(mod.isEnabled()) {
			matrices.translate(mod.getX(), mod.getY(), mod.getZ());
			matrices.scale(mod.getScale(), mod.getScale(), mod.getScale());
		}
	}
	
	@Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 2, shift = At.Shift.AFTER))
	private void applyFoodSwingOffset(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
			float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		applyOldSwingOffset(player, hand, swingProgress, matrices);
	}

	@Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 3, shift = At.Shift.AFTER))
	private void applyBlockingSwingOffset(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
			float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		applyOldSwingOffset(player, hand, swingProgress, matrices);
	}

	@Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 4, shift = At.Shift.AFTER))
	private void applyBowSwingOffset(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
			float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		applyOldSwingOffset(player, hand, swingProgress, matrices);
	}

	@Unique
	private void applyOldSwingOffset(AbstractClientPlayerEntity player, Hand hand, float swingProgress,
			MatrixStack matrices) {
		if (OldAnimationsMod.getInstance().isEnabled() && OldAnimationsMod.getInstance().isOldBreaking()) {
			final Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
			applySwingOffset(matrices, arm, swingProgress);
		}
	}
}
