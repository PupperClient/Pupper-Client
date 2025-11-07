package com.soarclient.management.mod.api.hud.design;

import java.awt.Color;

import io.github.humbleui.skija.Font;

public abstract class HUDDesign {

	private String name;

	public HUDDesign(String name) {
		this.name = name;
	}

	public abstract void drawBackground(float x, float y, float width, float height, float radius);

	public abstract void drawText(String text, float x, float y, Font font);

	public abstract Color getTextColor();

	public abstract Color getOnTextColor();

	public String getName() {
		return name;
	}
}