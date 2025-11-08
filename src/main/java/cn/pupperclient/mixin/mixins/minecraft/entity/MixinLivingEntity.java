package cn.pupperclient.mixin.mixins.minecraft.entity;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.EventFallFlying;
import cn.pupperclient.event.client.EventJump;
import cn.pupperclient.event.client.EventRotationAnimation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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



    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"
        ),
        method = {"turnHead"}
    )
    private float modifyHeadYaw(LivingEntity entity) {
        float yaw = entity.getYaw();

        if (entity instanceof PlayerEntity) {
            EventRotationAnimation event = new EventRotationAnimation(entity.getYaw(), 0.0F, 0.0F, 0.0F);
            EventBus.getInstance().post(event);
            return event.getYaw();
        } else {
            return yaw;
        }
    }

    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getYaw()F",
            opcode = 182,
            ordinal = 0
        ),
        method = {"jump"}
    )
    private float modifyJumpYaw(LivingEntity entity) {
        float yaw = entity.getYaw();
        if (entity instanceof PlayerEntity) {
            EventJump event = new EventJump(yaw);
            EventBus.getInstance().post(event);
            return event.getYaw();
        }
        return yaw;
    }

//    @Redirect(
//        method = "travelGliding",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/entity/LivingEntity;calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"
//        )
//    )
//    private Vec3d redirectCalcGlidingVelocity(LivingEntity instance, Vec3d oldVelocity) {
//        EventFallFlying event = new EventFallFlying(instance.getPitch());
//        EventBus.getInstance().post(event);
//
//        Vec3d originalVelocity = instance.calcGlidingVelocity(getVelocity());
//
//        if (event.getPitch() != instance.getPitch()) {
//            return originalVelocity;
//        }
//
//        return originalVelocity;
//    }
    @Redirect(
        method = "travelGliding",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"
        )
    )
    private Vec3d redirectCalcGlidingVelocity(LivingEntity instance, Vec3d velocity) {
        if (instance instanceof PlayerEntity) {
            EventFallFlying event = new EventFallFlying(instance.getPitch());
            EventBus.getInstance().post(event);
            return instance.calcGlidingVelocity(velocity);
        }
        return instance.calcGlidingVelocity(velocity);
    }
}
