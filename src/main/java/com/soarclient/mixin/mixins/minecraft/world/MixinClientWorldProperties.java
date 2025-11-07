package com.soarclient.mixin.mixins.minecraft.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.soarclient.management.mod.impl.misc.TimeChangerMod;

import net.minecraft.client.world.ClientWorld;

@Mixin(ClientWorld.Properties.class)
public class MixinClientWorldProperties {

	@Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
	public void getTimeOfDay(CallbackInfoReturnable<Long> cir) {
		if (TimeChangerMod.getInstance().isEnabled()) {
			cir.setReturnValue(TimeChangerMod.getInstance().getTime());
		}
	}
}
