package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.skia.font.Icon;

public class HealthDisplayMod extends SimpleHUDMod {

	public HealthDisplayMod() {
		super("mod.healthdisplay.name", "mod.healthdisplay.description", Icon.FAVORITE);
	}

	public EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {
        if (mc.player != null) {
            return (int) mc.player.getHealth() + " Health";
        }
        return "";
    }

	@Override
	public String getIcon() {
		return Icon.FAVORITE;
	}
}
