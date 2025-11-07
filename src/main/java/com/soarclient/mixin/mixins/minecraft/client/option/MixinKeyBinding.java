package com.soarclient.mixin.mixins.minecraft.client.option;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.soarclient.management.mod.impl.player.SnapTapMod;
import com.soarclient.mixin.interfaces.IMixinKeyBinding;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Mixin(KeyBinding.class)
public class MixinKeyBinding implements IMixinKeyBinding {

	@Shadow
	@Final
	private InputUtil.Key defaultKey;

	@Shadow
	private boolean pressed;

	@Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
	public void onGetPressed(CallbackInfoReturnable<Boolean> cir) {

		SnapTapMod mod = SnapTapMod.getInstance();

		if (mod == null || !mod.isEnabled()) {
			return;
		}

		if (this.defaultKey.getCode() == InputUtil.GLFW_KEY_A) {
			if (this.pressed) {
				if (mod.getRightPressTime() == 0) {
					cir.setReturnValue(true);
					cir.cancel();
					return;
				}

				cir.setReturnValue(mod.getRightPressTime() <= mod.getLeftPressTime());
				cir.cancel();
			}
		} else if (this.defaultKey.getCode() == InputUtil.GLFW_KEY_D) {
			if (this.pressed) {
				if (mod.getLeftPressTime() == 0) {
					cir.setReturnValue(true);
					cir.cancel();
					return;
				}

				cir.setReturnValue(mod.getLeftPressTime() <= mod.getRightPressTime());
				cir.cancel();
			}
		} else if (this.defaultKey.getCode() == InputUtil.GLFW_KEY_W) {
			if (this.pressed) {
				if (mod.getForwardPressTime() == 0) {
					cir.setReturnValue(true);
					cir.cancel();
					return;
				}

				cir.setReturnValue(mod.getBackPressTime() <= mod.getForwardPressTime());
				cir.cancel();
			}
		} else if (this.defaultKey.getCode() == InputUtil.GLFW_KEY_S) {
			if (this.pressed) {
				if (mod.getBackPressTime() == 0) {
					cir.setReturnValue(true);
					cir.cancel();
					return;
				}

				cir.setReturnValue(mod.getForwardPressTime() <= mod.getBackPressTime());
				cir.cancel();
			}
		}
	}

	@Inject(method = "setPressed", at = @At("HEAD"))
	public void setPressed(boolean pressed, CallbackInfo ci) {

		SnapTapMod mod = SnapTapMod.getInstance();

		if (mod == null || !mod.isEnabled()) {
			return;
		}

		if (this.defaultKey.getCode() == InputUtil.GLFW_KEY_A) {
			if (pressed) {
				mod.setLeftPressTime(System.currentTimeMillis());
			} else {
				mod.setLeftPressTime(0);
			}
		} else if (this.defaultKey.getCode() == InputUtil.GLFW_KEY_D) {
			if (pressed) {
				mod.setRightPressTime(System.currentTimeMillis());
			} else {
				mod.setRightPressTime(0);
			}
		} else if (this.defaultKey.getCode() == InputUtil.GLFW_KEY_W) {
			if (pressed) {
				mod.setForwardPressTime(System.currentTimeMillis());
			} else {
				mod.setForwardPressTime(0);
			}
		} else if (this.defaultKey.getCode() == InputUtil.GLFW_KEY_S) {
			if (pressed) {
				mod.setBackPressTime(System.currentTimeMillis());
			} else {
				mod.setBackPressTime(0);
			}
		}
	}

	@Override
	public boolean getRealIsPressed() {
		return this.pressed;
	}
}
