package com.soarclient.management.mod.impl.hud;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.management.mod.api.hud.SimpleHUDMod;
import com.soarclient.skia.font.Icon;

public class GameModeDisplayMod extends SimpleHUDMod {

	public GameModeDisplayMod() {
		super("mod.gamemodedisplay.name", "mod.gamemodedisplay.description", Icon.SPORTS_ESPORTS);
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {

		String prefix = "Mode: ";

		if (mc.player.isCreative()) {
			return prefix + "Creative";
		} else if (mc.player.isSpectator()) {
			return prefix + "Spectator";
		} else {
			return prefix + "Survival";
		}
	}

	@Override
	public String getIcon() {
		return Icon.SPORTS_ESPORTS;
	}
}