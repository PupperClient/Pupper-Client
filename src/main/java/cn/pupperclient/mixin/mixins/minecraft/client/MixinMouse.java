package cn.pupperclient.mixin.mixins.minecraft.client;

import cn.pupperclient.PupperClient;
import cn.pupperclient.event.client.MouseClickEvent;
import cn.pupperclient.management.mod.impl.hud.CPSDisplayMod;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.MouseScrollEvent;
import cn.pupperclient.management.mod.settings.impl.KeybindSetting;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
public abstract class MixinMouse {

	@Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V", shift = At.Shift.AFTER))
	public void onPressed(long window, int button, int action, int mods, CallbackInfo ci) {

		for (KeybindSetting s : PupperClient.getInstance().getModManager().getKeybindSettings()) {

			if (s.getKey().equals(Type.MOUSE.getOrCreate(button))) {

				if (action == GLFW.GLFW_PRESS) {
					s.setPressed();
				}

				s.setKeyDown(true);
			}
		}
	}

	@Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V", shift = At.Shift.AFTER, ordinal = 0))
	public void onReleased(long window, int button, int action, int mods, CallbackInfo ci) {
		for (KeybindSetting s : PupperClient.getInstance().getModManager().getKeybindSettings()) {
			if (s.getKey().equals(Type.MOUSE.getOrCreate(button))) {
				s.setKeyDown(false);
			}
		}
	}

    @Inject(method = "onPress", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            Minecraft client = Minecraft.getInstance();
            if (client == null) return;

            double rawX = client.mouseHandler.xpos();
            double rawY = client.mouseHandler.ypos();

            Window win = client.getWindow();
            double scaleFactor = win.getGuiScale();
            double scaledX = rawX / scaleFactor;
            double scaledY = win.getGuiScaledHeight() - (rawY / scaleFactor);

            EventBus.getInstance().post(new MouseClickEvent(button, scaledX, scaledY));
        }
    }

	@Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedHotbarSlot(I)V", shift = At.Shift.BEFORE), cancellable = true)
	private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {

		MouseScrollEvent event = new MouseScrollEvent(vertical);

		EventBus.getInstance().post(event);

		if (event.isCancelled()) {
			ci.cancel();
		}
	}

    @Inject(method = "onPress", at = @At("HEAD"))
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
