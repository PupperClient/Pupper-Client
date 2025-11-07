package com.soarclient.mixin.mixins.viafabricplus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.soarclient.management.mod.impl.player.OldAnimationsMod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;

@Mixin(value = MinecraftClient.class, priority = 2000)
public class MixinMinecraftClient {

	@Shadow
	public ClientPlayerEntity player;
	
	@Shadow
	public ClientPlayerInteractionManager interactionManager;
	
    @ModifyExpressionValue(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"))
    private boolean injectOldAnimation(boolean original) {
    	
    	if(OldAnimationsMod.getInstance().isEnabled()) {
    		return false;
    	}
    	
        return original;
    }
}
