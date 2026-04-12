package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.skia.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.skia.font.Icon;

import java.util.Objects;

public class PlayerCounterMod extends SimpleHUDMod {

	public PlayerCounterMod() {
		super("mod.playercounter.name", "mod.playercounter.description", Icon.GROUPS);
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {
        assert client.level != null;
        return "Player: " + Objects.requireNonNull(client.level.getServer()).getPlayerCount();
	}

	@Override
	public String getIcon() {
		return Icon.GROUPS;
	}
}
