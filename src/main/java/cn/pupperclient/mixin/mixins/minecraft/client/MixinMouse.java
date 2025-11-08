package cn.pupperclient.mixin.mixins.minecraft.client;

import cn.pupperclient.PupperClient;
import cn.pupperclient.event.client.MouseClickEvent;
import cn.pupperclient.management.mod.impl.hud.CPSDisplayMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.MouseScrollEvent;
import cn.pupperclient.management.mod.settings.impl.KeybindSetting;

import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil.Type;

@Mixin(Mouse.class)
public abstract class MixinMouse {

	@Inject(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V", shift = At.Shift.AFTER))
	public void onPressed(long window, int button, int action, int mods, CallbackInfo ci) {

		for (KeybindSetting s : PupperClient.getInstance().getModManager().getKeybindSettings()) {

			if (s.getKey().equals(Type.MOUSE.createFromCode(button))) {

				if (action == GLFW.GLFW_PRESS) {
					s.setPressed();
				}

				s.setKeyDown(true);
			}
		}
	}

	@Inject(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V", shift = At.Shift.AFTER, ordinal = 0))
	public void onReleased(long window, int button, int action, int mods, CallbackInfo ci) {
		for (KeybindSetting s : PupperClient.getInstance().getModManager().getKeybindSettings()) {
			if (s.getKey().equals(Type.MOUSE.createFromCode(button))) {
				s.setKeyDown(false);
			}
		}
	}

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) return;

            double rawX = client.mouse.getX();
            double rawY = client.mouse.getY();

            Window win = client.getWindow();
            double scaleFactor = win.getScaleFactor();
            double scaledX = rawX / scaleFactor;
            double scaledY = win.getScaledHeight() - (rawY / scaleFactor);

            EventBus.getInstance().post(new MouseClickEvent(button, scaledX, scaledY));
        }
    }

	@Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;setSelectedSlot(I)V", shift = At.Shift.BEFORE), cancellable = true)
	private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {

		MouseScrollEvent event = new MouseScrollEvent(vertical);

		EventBus.getInstance().post(event);

		if (event.isCancelled()) {
			ci.cancel();
		}
	}

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButtonForCPS(long window, int button, int action, int mods, CallbackInfo ci) {
        CPSDisplayMod cpsDisplayMod = PupperClient.getInstance().getModManager().getMods()
            .stream()
            .filter(mod -> mod instanceof CPSDisplayMod)
            .map(mod -> (CPSDisplayMod) mod)
            .findFirst()
            .orElse(null);

        if (cpsDisplayMod != null && action == GLFW.GLFW_PRESS) {
            cpsDisplayMod.onMouseClick(button, true);
        }
    }
}
