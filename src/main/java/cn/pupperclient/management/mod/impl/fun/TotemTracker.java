package cn.pupperclient.management.mod.impl.fun;

import cn.pupperclient.management.mod.Mod;
import cn.pupperclient.management.mod.ModCategory;
import cn.pupperclient.skia.font.Icon;

public class TotemTracker extends Mod {
    private static TotemTracker instance;

    public TotemTracker() {
        super("mod.totemtracker.name", "mod.totemtracker.description", Icon.SECURITY, ModCategory.FUN);
    }

    public static TotemTracker getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
