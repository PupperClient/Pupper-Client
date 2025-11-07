package com.soarclient.management.mod.impl.hud;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.management.mod.api.hud.SimpleHUDMod;
import com.soarclient.management.mod.settings.impl.NumberSetting;
import com.soarclient.skia.font.Icon;
import com.soarclient.utils.Multithreading;
import com.soarclient.utils.TimerUtils;
import com.soarclient.utils.server.ServerUtils;

import net.lenni0451.mcping.MCPing;

import java.util.Objects;

public class PingDisplayMod extends SimpleHUDMod {

	private TimerUtils timer = new TimerUtils();
	private NumberSetting refreshTimeSetting = new NumberSetting("setting.refreshtime",
			"setting.refreshtime.description", Icon.REFRESH, this, 4, 1, 20, 1);
	private long ping;
	private boolean pinging;

	public PingDisplayMod() {
		super("mod.pingdisplay.name", "mod.pingdisplay.description", Icon.WIFI);
		pinging = false;
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	private void updatePing() {

		if (timer.delay((long) (1000 * refreshTimeSetting.getValue()))) {

			if (ServerUtils.isMultiplayer()) {
				if (Objects.requireNonNull(mc.getCurrentServerEntry()).ping <= 1 && !pinging) {
					Multithreading.runAsync(() -> {
						pinging = true;
						ping = MCPing.pingModern().address(mc.getCurrentServerEntry().address).getSync().getPing();
						pinging = false;
					});
				} else {
					ping = mc.getCurrentServerEntry().ping;
				}
			} else if (mc.isIntegratedServerRunning()) {
				ping = 0;
			}

			timer.reset();
		}
	}

	@Override
	public String getText() {
		updatePing();
		return ping + " ms";
	}

	@Override
	public String getIcon() {
		return Icon.WIFI;
	}
}