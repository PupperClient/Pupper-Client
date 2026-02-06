package cn.pupperclient.mixin.mixins.minecraft.entity;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.PlayerDirectionChangeEvent;
import cn.pupperclient.mixin.interfaces.IMixinCameraEntity;

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

	@Override
	@Unique
	public float pupper$getCameraPitch() {
		return this.cameraPitch;
	}

	@Override
	@Unique
	public float pupper$getCameraYaw() {
		return this.cameraYaw;
	}

	@Override
	@Unique
	public void pupper$setCameraPitch(float pitch) {
		this.cameraPitch = pitch;
	}

	@Override
	@Unique
	public void pupper$setCameraYaw(float yaw) {
		this.cameraYaw = yaw;
	}
}
