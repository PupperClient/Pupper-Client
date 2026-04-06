package cn.pupperclient.mixin.mixins.minecraft.client;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.PupperClient;
import cn.pupperclient.management.mod.settings.impl.KeybindSetting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboard {

    @Inject(
        method = "keyPress(JIIII)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V",
            shift = At.Shift.AFTER
        )
    )
    private void onKeyPressed(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            InputConstants.Key inputKey = InputConstants.getKey(key, scancode);
            for (KeybindSetting setting : PupperClient.getInstance().getModManager().getKeybindSettings()) {
                if (setting.getKey().equals(inputKey)) {
                    setting.setPressed();
                    setting.setKeyDown(true);
                }
            }
        }
    }

    @Inject(
        method = "keyPress(JIIII)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V",
            shift = At.Shift.AFTER
        )
    )
    private void onKeyReleased(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == GLFW.GLFW_RELEASE) {
            InputConstants.Key inputKey = InputConstants.getKey(key, scancode);
            for (KeybindSetting setting : PupperClient.getInstance().getModManager().getKeybindSettings()) {
                if (setting.getKey().equals(inputKey)) {
                    setting.setKeyDown(false);
                }
            }
        }
    }

    @Inject(
        at = {@At("HEAD")},
        method = {"keyPress"}
    )
    private void onKeyPress(long pWindowPointer, int pKey, int pScanCode, int pAction, int pModifiers, CallbackInfo ci) {
        if (pKey != -1 && PupperClient.getInstance() != null && EventBus.getInstance() != null) {
            EventBus.getInstance().post(new KeyEvent(pKey, pAction != 0));
        }
    }
}
