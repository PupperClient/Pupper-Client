package com.soarclient.management.mod.impl.player;

import com.soarclient.event.EventListener;
import com.soarclient.event.client.KeyEvent;
import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.skia.font.Icon;
import org.lwjgl.glfw.GLFW;

public class Sprint extends Mod {
    public Sprint() {
        super("mod.sprint.name", "mod.sprint.description", Icon.DIRECTIONS_RUN, ModCategory.PLAYER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onKeyPress(KeyEvent event) {
        if (event.getKey() == GLFW.GLFW_KEY_W) {
            mc.options.sprintKey.setPressed(true);
        } else if (event.getKey() == GLFW.GLFW_KEY_S) {
            mc.options.sprintKey.setPressed(false);
        }
    }
}
