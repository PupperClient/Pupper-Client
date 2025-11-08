package cn.pupperclient.management.mod.impl.player;

import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.client.KeyEvent;
import cn.pupperclient.management.mod.Mod;
import cn.pupperclient.management.mod.ModCategory;
import cn.pupperclient.skia.font.Icon;
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
