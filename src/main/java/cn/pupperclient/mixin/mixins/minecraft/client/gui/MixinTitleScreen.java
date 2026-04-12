package cn.pupperclient.mixin.mixins.minecraft.client.gui;

import cn.pupperclient.gui.MainMenuGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TitleScreen.class, priority = 1001)
public abstract class MixinTitleScreen extends Screen {

    protected MixinTitleScreen(Component title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
    public void onInit(CallbackInfo ci) {
//        Minecraft.getInstance().setScreen(new MainMenuGui());
//        ci.cancel();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    public void onRender(CallbackInfo ci) {
//        ci.cancel();
    }
}
