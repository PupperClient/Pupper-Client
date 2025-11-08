package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.skia.font.Icon;

public class PlayerCounterMod extends SimpleHUDMod {

	public PlayerCounterMod() {
		super("mod.playercounter.name", "mod.playercounter.description", Icon.GROUPS);
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {
		return "Player: " + mc.world.getPlayers().size();
	}

	@Override
	public String getIcon() {
		return Icon.GROUPS;
	}
}
