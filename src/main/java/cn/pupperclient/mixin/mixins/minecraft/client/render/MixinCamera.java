package cn.pupperclient.mixin.mixins.minecraft.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.management.mod.impl.player.FreelookMod;
import cn.pupperclient.mixin.interfaces.IMixinCameraEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Unique
    boolean firstTime = true;

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);
    
    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 1, shift = At.Shift.AFTER))
    public void lockRotation(BlockGetter focusedBlock, Entity cameraEntity, boolean isThirdPerson, boolean isFrontFacing, float tickDelta, CallbackInfo ci) {
    	
    	Minecraft client = Minecraft.getInstance();
    	
        if (FreelookMod.getInstance().isEnabled() && FreelookMod.getInstance().isActive() && cameraEntity instanceof LocalPlayer) {
        	IMixinCameraEntity cameraOverriddenEntity = (IMixinCameraEntity) cameraEntity;

            if (firstTime && Minecraft.getInstance().player != null) {
                cameraOverriddenEntity.soarClient_CN$setCameraPitch(client.player.getXRot());
                cameraOverriddenEntity.soarClient_CN$setCameraYaw(client.player.getYRot());
                firstTime = false;
            }
            this.setRotation(cameraOverriddenEntity.soarClient_CN$getCameraYaw(), cameraOverriddenEntity.soarClient_CN$getCameraPitch());

        }
        if (FreelookMod.getInstance().isEnabled() && !FreelookMod.getInstance().isActive() && cameraEntity instanceof LocalPlayer) {
            firstTime = true;
        }
    }
}
