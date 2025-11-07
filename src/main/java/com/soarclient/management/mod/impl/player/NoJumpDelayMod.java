package com.soarclient.management.mod.impl.player;

import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.skia.font.Icon;

public class NoJumpDelayMod extends Mod {
    private static NoJumpDelayMod instance;

	public NoJumpDelayMod() {
		super("mod.nojumpdelay.name", "mod.nojumpdelay.description", Icon.KEYBOARD_DOUBLE_ARROW_UP, ModCategory.PLAYER);

		instance = this;
	}

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public static NoJumpDelayMod getInstance() {
        return instance;
    }
}
