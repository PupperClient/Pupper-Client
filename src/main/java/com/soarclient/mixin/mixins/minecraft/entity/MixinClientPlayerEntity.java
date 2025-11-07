package com.soarclient.mixin.mixins.minecraft.entity;

import com.mojang.authlib.GameProfile;
import com.soarclient.event.EventBus;
import com.soarclient.event.EventType;
import com.soarclient.event.client.EventMotion;
import com.soarclient.event.client.EventUpdate;
import com.soarclient.utils.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayerEntity.class, priority = 1001)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements IMinecraft {
    @Unique
    private boolean wasSprinting;
    @Unique
    @Final
    public ClientConnection connection;
    @Unique
    private boolean wasShiftKeyDown;
    @Unique
    private double xLast;
    @Unique
    private double yLast1;
    @Unique
    private double zLast;
    @Unique
    private float yRotLast;
    @Unique
    private float xRotLast;
    @Unique
    private int positionReminder;
    @Shadow
    private boolean lastOnGround;
    @Shadow
    private boolean autoJumpEnabled;

    @Shadow
    public boolean lastSneaking;

    @Shadow
    public abstract void tick();

    @Shadow
    public abstract void sendSprintingPacket();

    @Shadow
    @Final
    protected MinecraftClient client;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Unique
    protected abstract boolean isControlledCamera();
    @Unique
    protected abstract void sendIsSprintingIfNeeded();

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void onSendMovementPacketsHead(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
        EventMotion eventPre = new EventMotion(EventType.PRE, self.getX(), self.getY(), self.getZ(), self.getYaw(), self.getPitch(), self.isOnGround());
        EventBus.getInstance().post(eventPre);
        if (eventPre.isCancelled()) {
            EventBus.getInstance().post(new EventMotion(EventType.POST, eventPre.getYaw(), eventPre.getPitch()));
            ci.cancel();
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getX()D"))
    private double redirectGetX(ClientPlayerEntity instance) {
        EventMotion event = new EventMotion(EventType.PRE, instance.getX(), instance.getY(), instance.getZ(), instance.getYaw(), instance.getPitch(), instance.isOnGround());
        EventBus.getInstance().post(event);
        return event.getX();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getY()D"))
    private double redirectGetY(ClientPlayerEntity instance) {
        EventMotion event = new EventMotion(EventType.PRE, instance.getX(), instance.getY(), instance.getZ(), instance.getYaw(), instance.getPitch(), instance.isOnGround());
        EventBus.getInstance().post(event);
        return event.getY();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D"))
    private double redirectGetZ(ClientPlayerEntity instance) {
        EventMotion event = new EventMotion(EventType.PRE, instance.getX(), instance.getY(), instance.getZ(), instance.getYaw(), instance.getPitch(), instance.isOnGround());
        EventBus.getInstance().post(event);
        return event.getZ();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
    private float redirectGetYaw(ClientPlayerEntity instance) {
        EventMotion event = new EventMotion(EventType.PRE, instance.getX(), instance.getY(), instance.getZ(), instance.getYaw(), instance.getPitch(), instance.isOnGround());
        EventBus.getInstance().post(event);
        return event.getYaw();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isOnGround()Z"))
    private boolean redirectIsOnGround(ClientPlayerEntity instance) {
        EventMotion event = new EventMotion(EventType.PRE, instance.getX(), instance.getY(), instance.getZ(), instance.getYaw(), instance.getPitch(), instance.isOnGround());
        EventBus.getInstance().post(event);
        return event.isOnGround();
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"))
    private void onSendMovementPacketsTail(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
        EventBus.getInstance().post(new EventMotion(EventType.POST, self.getYaw(), self.getPitch()));
    }

    @Inject(
        method = {"tick"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V",
            shift = At.Shift.BEFORE
        )}
    )
    public void injectUpdateEvent(CallbackInfo ci) {
        EventBus.getInstance().post(new EventUpdate());
    }
}
