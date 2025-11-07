package com.soarclient.mixin.mixins.minecraft.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.soarclient.utils.IMinecraft.mc;

@Mixin(value = LanguageOptionsScreen.class, priority = 1001)
public class MixinLanguageScreen extends Screen {
    protected MixinLanguageScreen(Text title) {
        super(title);
    }

    @Inject(method = "onDone", at = @At("TAIL"))
    public void onDone(CallbackInfo ci) {
        mc.inGameHud.getChatHud().reset();
    }
}
