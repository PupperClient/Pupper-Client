package cn.pupperclient.mixin.mixins.minecraft.client;

import java.io.File;
import java.io.IOException;

import cn.pupperclient.skia.event.EventSkiaInit;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.pupperclient.PupperClient;
import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.ClientTickEvent;
import cn.pupperclient.event.client.GameLoopEvent;
import cn.pupperclient.libraries.browser.JCefBrowser;
import cn.pupperclient.management.config.ConfigType;
import cn.pupperclient.management.mod.impl.player.HitDelayFixMod;
import cn.pupperclient.management.mod.impl.player.OldAnimationsMod;
import cn.pupperclient.mixin.interfaces.IMixinLivingEntity;
import cn.pupperclient.mixin.interfaces.IMixinMinecraftClient;
import cn.pupperclient.skia.context.SkiaContext;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(value = MinecraftClient.class, priority = 300)
public abstract class MixinMinecraftClient implements IMixinMinecraftClient {

	@Shadow
	@Final
	private Window window;

	@Shadow
	public int attackCooldown;

	@Shadow
	public ClientPlayerInteractionManager interactionManager;

	@Final
	@Shadow
	public ParticleManager particleManager;

	@Shadow
	public GameOptions options;

	@Shadow
	public HitResult crosshairTarget;

	@Shadow
	public ClientWorld world;

	@Shadow
	public ClientPlayerEntity player;

	@Shadow
    protected abstract String getWindowTitle();

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Unique
	private File assetDir;

	@Inject(method = "<init>(Lnet/minecraft/client/RunArgs;)V", at = @At("TAIL"))
	public void onInit(RunArgs args, CallbackInfo ci) {
		assetDir = args.directories.assetDir;
	}

	@Inject(method = "stop", at = @At("HEAD"))
	public void onStop(CallbackInfo ci) {
		PupperClient.getInstance().getConfigManager().save(ConfigType.MOD);
		JCefBrowser.close();
	}

	@Inject(method = "handleBlockBreaking", at = @At("HEAD"))
	private void handleBlockBreaking(boolean breaking, CallbackInfo ci) {

		if (OldAnimationsMod.getInstance().isEnabled() && OldAnimationsMod.getInstance().isOldBreaking()) {
			if (this.options.attackKey.isPressed() && this.options.useKey.isPressed()) {

				if (breaking && this.crosshairTarget != null && this.crosshairTarget.getType() == Type.BLOCK) {

					BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;
					BlockPos blockPos = blockHitResult.getBlockPos();

					if (!this.world.getBlockState(blockPos).isAir()) {
						Direction direction = blockHitResult.getSide();
						this.particleManager.addBlockBreakingParticles(blockPos, direction);
						((IMixinLivingEntity) player).pupper$fakeSwingHand(Hand.MAIN_HAND);
					}
				}
			}
		}
	}

	@Inject(method = "doAttack", at = @At("HEAD"))
	private void onHitDelayFix(CallbackInfoReturnable<Boolean> cir) {
		if (HitDelayFixMod.getInstance().isEnabled()) {
			attackCooldown = 0;
		}
	}

	/**
	 * @author EldoDebug
	 * @reason updateWindowTitle
	 */
	@Overwrite
	public void updateWindowTitle() {
		this.window.setTitle(PupperClient.getInstance().getName() + " | " + PupperClient.getInstance().getVersion() + " for "
				+ getWindowTitle());
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(CallbackInfo ci) throws IOException {
        SkiaContext instance = SkiaContext.INSTANCE;

        int[] width = new int[1];
        int[] height = new int[1];

        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        GLFW.glfwGetFramebufferSize(windowHandle, width, height);

        int finalWidth = Math.max(width[0], 1);
        int finalHeight = Math.max(height[0], 1);

        EventBus.getInstance().post(new EventSkiaInit(finalWidth, finalHeight));

        PupperClient.getInstance().start();
	}

    @Inject(method = "stop", at = @At("HEAD"))
    public void onShutdown(CallbackInfo ci) {
        PupperClient.getInstance().onShutdown();
    }

	@Inject(method = "tick", at = @At("HEAD"))
	public void onClientTick(CallbackInfo ci) {
		EventBus.getInstance().post(new ClientTickEvent());
	}

	@Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport()V"))
	public void onGameLoop(CallbackInfo ci) {
		EventBus.getInstance().post(new GameLoopEvent());
	}

	@Override
	public File getAssetDir() {
		return this.assetDir;
	}
}
