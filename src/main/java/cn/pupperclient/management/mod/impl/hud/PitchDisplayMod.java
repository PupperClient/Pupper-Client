package cn.pupperclient.management.mod.impl.hud;

import java.text.DecimalFormat;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.skia.font.Icon;

public class PitchDisplayMod extends SimpleHUDMod {

	private DecimalFormat df = new DecimalFormat("0.##");

	public PitchDisplayMod() {
		super("mod.pitchdisplay.name", "mod.pitchdisplay.description", Icon.ARROW_UPWARD);
	}

	public EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {
		return "Pitch: " + df.format(mc.player.getPitch());
	}

	@Override
	public String getIcon() {
		return Icon.ARROW_UPWARD;
	}
}
