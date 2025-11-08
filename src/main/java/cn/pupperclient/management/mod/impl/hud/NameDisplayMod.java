package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.skia.font.Icon;

public class NameDisplayMod extends SimpleHUDMod {

	public NameDisplayMod() {
		super("mod.namedisplay.name", "mod.namedisplay.description", Icon.PERSON);
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {
		return "Name: " + mc.player.getGameProfile().getName();
	}

	@Override
	public String getIcon() {
		return Icon.PERSON;
	}
}
