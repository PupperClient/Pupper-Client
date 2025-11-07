package com.soarclient.management.mod.impl.hud;

import com.soarclient.management.mod.api.hud.HUDMod;
import com.soarclient.management.mod.settings.impl.BooleanSetting;
import com.soarclient.skia.font.Icon;

public class BossBarMod extends HUDMod {

	private static BossBarMod instance;
	private BooleanSetting vanillaPosition = new BooleanSetting("setting.vanillaposition",
			"setting.vanillaposition.description", Icon.PICTURE_IN_PICTURE_CENTER, this, true);

	public BossBarMod() {
		super("mod.bossbar.name", "mod.bossbar.description", Icon.BATTERY_LOW);

		instance = this;
	}

	public static BossBarMod getInstance() {
		return instance;
	}
	
	public boolean isVanillaPosition() {
		return vanillaPosition.isEnabled();
	}
	
	@Override
	public float getRadius() {
		return 0;
	}
}
