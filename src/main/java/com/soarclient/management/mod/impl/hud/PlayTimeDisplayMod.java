package com.soarclient.management.mod.impl.hud;

import com.soarclient.Soar;
import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.management.mod.api.hud.SimpleHUDMod;
import com.soarclient.skia.font.Icon;

public class PlayTimeDisplayMod extends SimpleHUDMod {

	public PlayTimeDisplayMod() {
		super("mod.playtimedisplay.name", "mod.playtimedisplay.description", Icon.WATCH);
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {

		int sec = (int) ((System.currentTimeMillis() - Soar.getInstance().getLaunchTime()) / 1000);
		int min = (sec % 3600) / 60;
		int hour = sec / 3600;
		sec = sec % 60;

		return String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec);
	}

	@Override
	public String getIcon() {
		return Icon.WATCH;
	}
}