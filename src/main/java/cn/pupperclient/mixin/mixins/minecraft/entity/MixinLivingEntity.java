package cn.pupperclient.mixin.mixins.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.management.mod.impl.hud.JumpResetIndicatorMod;
import cn.pupperclient.management.mod.impl.player.NoJumpDelayMod;
import cn.pupperclient.mixin.interfaces.IMixinLivingEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements IMixinLivingEntity{

	@Shadow
	private int jumpingCooldown;

	@Shadow
	public int handSwingTicks;

	@Shadow
	public boolean handSwinging;

    @Shadow
	public Hand preferredHand;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract int getHandSwingDuration();

    @Shadow
    public abstract void jump();

    @Shadow
    public abstract float getYaw(float tickDelta);

    @Shadow
    protected abstract void initDataTracker(DataTracker.Builder builder);

    @Inject(method = "tickMovement", at = @At("HEAD"))
	public void onNoJumpDelay(CallbackInfo ci) {
		if (NoJumpDelayMod.getInstance().isEnabled()) {
			jumpingCooldown = 0;
		}
	}

	@Inject(method = "jump", at = @At("HEAD"))
	private void onJump(CallbackInfo info) {

		JumpResetIndicatorMod mod = JumpResetIndicatorMod.getInstance();
		MinecraftClient client = MinecraftClient.getInstance();

		if ((Object) this == client.player) {
			mod.setJumpAge(client.player.age);
			mod.setLastTime(System.currentTimeMillis());
		}
	}

	@Inject(method = "onDamaged", at = @At("HEAD"))
	private void onDamage(CallbackInfo info) {

		JumpResetIndicatorMod mod = JumpResetIndicatorMod.getInstance();
		MinecraftClient client = MinecraftClient.getInstance();

		if ((Object) this == client.player) {
			mod.setHurtAge(client.player.age);
		}
	}

	@Override
	public void soarClient_CN$fakeSwingHand(Hand hand) {
		if (!this.handSwinging || this.handSwingTicks >= this.getHandSwingDuration() / 2 || this.handSwingTicks < 0) {
			this.handSwingTicks = -1;
			this.handSwinging = true;
			this.preferredHand = hand;
		}
	}
}
