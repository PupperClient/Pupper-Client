package com.soarclient.management.mod.impl.player;

import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.skia.font.Icon;

public class HitDelayFixMod extends Mod {

	private static HitDelayFixMod instance;

	public HitDelayFixMod() {
		super("mod.hitdelayfix.name", "mod.hitdelayfix.description", Icon.TIMER_OFF, ModCategory.PLAYER);

		instance = this;
	}

	public static HitDelayFixMod getInstance() {
		return instance;
	}
}
