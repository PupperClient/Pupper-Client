package com.soarclient.management.mod.impl.hud;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.libraries.browser.JCefBrowser;
import com.soarclient.management.mod.api.hud.HUDMod;
import com.soarclient.management.mod.settings.impl.NumberSetting;
import com.soarclient.skia.Skia;
import com.soarclient.skia.font.Icon;

import net.ccbluex.liquidbounce.mcef.MCEF;

public class WebBrowserMod extends HUDMod {

	private static WebBrowserMod instance;
	private float currentWidth, currentHeight;
	private NumberSetting opacitySetting = new NumberSetting("setting.opacity", "setting.opacity.description",
			Icon.OPACITY, this, 1, 0.1F, 1, 0.1F);

	public WebBrowserMod() {
		super("mod.webbrowser.name", "mod.webbrowser.description", Icon.LANGUAGE);
		instance = this;
	}

	public EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {

		float scale = position.getScale();
		float width = 1920 * 0.11F;
		float height = 1080 * 0.11F;
		
		currentWidth = width * scale;
		currentHeight = height * scale;

		if (MCEF.INSTANCE.isInitialized() && JCefBrowser.getBrowser() != null) {
			MCEF.INSTANCE.getApp().getHandle().N_DoMessageLoopWork();
			this.drawBlurBackground(getX(), getY(), currentWidth, currentHeight);
			Skia.drawRoundedImage(JCefBrowser.getBrowser().getRenderer().getTextureID(), getX(), getY(), currentWidth, currentHeight,
					getRadius(), opacitySetting.getValue());
		}

		position.setSize(width, height);
	};

	@Override
	public float getRadius() {
		return 8;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		JCefBrowser.init();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		JCefBrowser.close();
	}

	public static WebBrowserMod getInstance() {
		return instance;
	}
	
	public int getMouseX(double mouseX) {
	    double relativeX = (mouseX - position.getX()) / currentWidth;
	    return (int)(relativeX * 1280);
	}

	public int getMouseY(double mouseY) {
	    double relativeY = (mouseY - position.getY()) / currentHeight;
	    return (int)(relativeY * 720);
	}
}
