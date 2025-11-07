package com.soarclient.mixin.mixins.minecraft.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.soarclient.management.mod.impl.render.FullbrightMod;

import net.minecraft.client.render.LightmapTextureManager;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    @ModifyExpressionValue(method = "update(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 1))
    private Object injectFullBright(Object original) {
        if (FullbrightMod.getInstance().isEnabled()) {
            return (double) FullbrightMod.getInstance().getGamma();
        }
        return original;
    }
}
