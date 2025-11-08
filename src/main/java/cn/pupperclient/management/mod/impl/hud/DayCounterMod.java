package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.skia.font.Icon;

public class DayCounterMod extends SimpleHUDMod {

	public DayCounterMod() {
		super("mod.daycounter.name", "mod.daycounter.description", Icon.TODAY);
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {
		long time = mc.world.getTime() / 24000L;
		return time + " Day" + (time > 1L ? "s" : "");
	}

	@Override
	public String getIcon() {
		return Icon.TODAY;
	}
}
