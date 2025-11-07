package com.soarclient.management.mod.impl.render;

import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.management.mod.settings.impl.NumberSetting;
import com.soarclient.skia.font.Icon;

public class FullbrightMod extends Mod {

	private static FullbrightMod instance;
	private NumberSetting gammaSetting = new NumberSetting("setting.gamma", "setting.gamma.description", Icon.LIGHTBULB,
			this, 15, 1, 15, 1);

	public FullbrightMod() {
		super("mod.fullbright.name", "mod.fullbright.description", Icon.LIGHTBULB, ModCategory.RENDER);
		instance = this;
	}

	public static FullbrightMod getInstance() {
		return instance;
	}
	
	public float getGamma() {
		return gammaSetting.getValue();
	}
}
