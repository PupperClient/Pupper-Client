package cn.pupperclient.mixin.mixins.minecraft.entity;

import cn.pupperclient.event.client.EventRayTrace;
import cn.pupperclient.event.client.EventStrafe;
import cn.pupperclient.management.mod.impl.fun.TotemTracker;
import cn.pupperclient.utils.ChatUtils;
import cn.pupperclient.utils.SoundEventHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.PlayerDirectionChangeEvent;
import cn.pupperclient.management.mod.impl.player.FreelookMod;
import cn.pupperclient.mixin.interfaces.IMixinCameraEntity;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Mixin(Entity.class)
public abstract class MixinEntity implements IMixinCameraEntity {
    @Unique
    protected Vec3d stuckSpeedMultiplier;

	@Unique
	private float cameraPitch;

	@Unique
	private float cameraYaw;

	@Shadow
	public abstract float getPitch();

	@Shadow
	public abstract float getYaw();

    @Shadow
    public abstract boolean equals(Object o);

    @Shadow
    public abstract float getPitch(float tickDelta);

    @Shadow
    public abstract float getYaw(float tickDelta);

    @Shadow
    public abstract Vec3d getRotationVector(float pitch, float yaw);

    @Shadow
    public abstract boolean saveNbt(NbtCompound nbt);

    @Shadow
    public abstract int getId();

    @Inject(method = "changeLookDirection", at = @At("HEAD"))
	private void onPlayerDirectionChange(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {

		float prevPitch = getPitch();
		float prevYaw = getYaw();
		float pitch = prevPitch + (float) (cursorDeltaY * .15);
		float yaw = prevYaw + (float) (cursorDeltaX * .15);
		pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);

		EventBus.getInstance().post(new PlayerDirectionChangeEvent(prevPitch, prevYaw, pitch, yaw));
	}

	@Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
	public void changeCameraLookDirection(double xDelta, double yDelta, CallbackInfo ci) {
		if (FreelookMod.getInstance().isEnabled() && FreelookMod.getInstance().isActive()
				&& (Entity) (Object) this instanceof ClientPlayerEntity) {
			double pitchDelta = (yDelta * 0.15);
			double yawDelta = (xDelta * 0.15);

			this.cameraPitch = MathHelper.clamp(this.cameraPitch + (float) pitchDelta, -90.0f, 90.0f);
			this.cameraYaw += (float) yawDelta;

			ci.cancel();

		}
	}

	@Override
	@Unique
	public float soarClient_CN$getCameraPitch() {
		return this.cameraPitch;
	}

	@Override
	@Unique
	public float soarClient_CN$getCameraYaw() {
		return this.cameraYaw;
	}

	@Override
	@Unique
	public void soarClient_CN$setCameraPitch(float pitch) {
		this.cameraPitch = pitch;
	}

	@Override
	@Unique
	public void soarClient_CN$setCameraYaw(float yaw) {
		this.cameraYaw = yaw;
	}

    @Inject(method = "playSound", at = @At("HEAD"))
    private void onPlaySound(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        SoundEventHelper.lastSoundSource = entity;
        SoundEventHelper.lastSoundEvent = sound;

        if (sound.id().toString().equals("minecraft:random.pop") && TotemTracker.getInstance().isEnabled()) {
            ChatUtils.addChatMessage("玩家 " + entity.getName().getString() + " 触发了不死图腾");
        }
    }

    /**
     * @author a
     * @reason a
     */
    @Overwrite
    public final Vec3d getRotationVec(float tick) {
        float pitch = this.getPitch(tick);
        float yaw = this.getYaw(tick);
        Entity thisEntity = (Entity)(Object)this;

        if (MinecraftClient.getInstance().player != null && thisEntity.getId() == MinecraftClient.getInstance().player.getId()) {
            EventRayTrace lookEvent = new EventRayTrace(MinecraftClient.getInstance().player, yaw, pitch);
            EventBus.getInstance().post(lookEvent);
            yaw = lookEvent.yaw;
            pitch = lookEvent.pitch;
        }

        return this.getRotationVector(pitch, yaw);
    }

    @ModifyArg(
        method = {"updateVelocity"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;",
            ordinal = 0
        ),
        index = 2
    )
    private float modifyYaw(float yaw) {
        Entity thisEntity = (Entity) (Object) this;

        if (MinecraftClient.getInstance().player != null && thisEntity.getId() == MinecraftClient.getInstance().player.getId()) {
            EventStrafe strafe = new EventStrafe(yaw);
            EventBus.getInstance().post(strafe);
            return strafe.getYaw();
        }

        return yaw;
    }
}
