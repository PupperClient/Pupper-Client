package com.soarclient.mixin.mixins.minecraft.client.gui;

import com.soarclient.event.EventBus;
import com.soarclient.event.server.ChatEvent;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class MixinChatHud {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void onChatMessage(Text message, CallbackInfo ci) {
        String rawMessage = message.getString();
        ChatEvent event = new ChatEvent(rawMessage);
        EventBus.getInstance().post(event);
    }
}
