package cn.pupperclient.mixin.mixins.minecraft.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.LightTexture;
import cn.pupperclient.management.mod.impl.render.FullbrightMod;

@Mixin(LightTexture.class)
public class MixinLightmapTextureManager {

    @ModifyExpressionValue(method = "updateLightTexture(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 1))
    private Object injectFullBright(Object original) {
        if (FullbrightMod.getInstance().isEnabled()) {
            return (double) FullbrightMod.getInstance().getGamma();
        }
        return original;
    }
}
