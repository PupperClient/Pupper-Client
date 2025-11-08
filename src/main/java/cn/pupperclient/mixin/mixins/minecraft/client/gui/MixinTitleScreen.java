package cn.pupperclient.mixin.mixins.minecraft.client.gui;

import cn.pupperclient.gui.MainMenuGui;
import cn.pupperclient.gui.welcomegui.WelcomeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

@Mixin(value = TitleScreen.class, priority = 1001)
public abstract class MixinTitleScreen extends Screen {

    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
    public void onInit(CallbackInfo ci) {
        WelcomeGui welcomeGui = new WelcomeGui();
        welcomeGui.setNextScreen(new MainMenuGui().build());
        MinecraftClient.getInstance().setScreen(welcomeGui.build());
        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(CallbackInfo ci) {
        ci.cancel();
    }
}
