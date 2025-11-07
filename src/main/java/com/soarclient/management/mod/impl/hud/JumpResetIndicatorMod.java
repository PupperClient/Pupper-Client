package com.soarclient.management.mod.impl.hud;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.management.mod.api.hud.SimpleHUDMod;
import com.soarclient.management.mod.settings.impl.NumberSetting;
import com.soarclient.skia.font.Icon;

public class JumpResetIndicatorMod extends SimpleHUDMod {

	private static JumpResetIndicatorMod instance;

    private NumberSetting tickSetting = new NumberSetting("setting.maxtick", "setting.maxtick.description",
			Icon.SCHEDULE, this, 10, 1, 100, 1);

    private int hurtAge, jumpAge;

    private long lastTime;

	public JumpResetIndicatorMod() {
		super("mod.jumpresetindicator.name", "mod.jumpresetindicator.description", Icon.SPORTS_KABADDI);
		instance = this;
	}

	public EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {

		String diff = "No Jump";

		if (lastTime + 2500 <= System.currentTimeMillis() || Math.abs(jumpAge - hurtAge) >= tickSetting.getValue()) {
			diff = "No Jump";
		} else if (jumpAge == hurtAge + 1) {
			diff = "Perfect!";
		} else if (hurtAge + 1 < jumpAge) {
			diff = "Late: ".concat(String.valueOf(jumpAge - hurtAge + 1)).concat(" Tick");
		} else if (hurtAge + 1 > jumpAge) {
			diff = "Early: ".concat(String.valueOf(hurtAge + 1 - jumpAge)).concat(" Tick");
		}

		return diff;
	}

	@Override
	public String getIcon() {
		return Icon.SPORTS_KABADDI;
	}

	public static JumpResetIndicatorMod getInstance() {
		return instance;
	}

	public void setHurtAge(int hurtAge) {
		this.hurtAge = hurtAge;
	}

	public void setJumpAge(int jumpAge) {
		this.jumpAge = jumpAge;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
}
