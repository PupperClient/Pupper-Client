package cn.pupperclient.management.mod.impl.hud;

import java.text.DecimalFormat;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.skia.font.Icon;

public class YawDisplayMod extends SimpleHUDMod {

	private DecimalFormat df = new DecimalFormat("0.##");

	public YawDisplayMod() {
		super("mod.yawdisplay.name", "mod.yawdisplay.description", Icon.ARROW_RANGE);
	}

	public EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {
		return "Yaw: " + df.format(Math.abs(mc.player.getYaw() % 90));
	}

	@Override
	public String getIcon() {
		return Icon.ARROW_RANGE;
	}
}
