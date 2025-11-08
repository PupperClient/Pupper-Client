package cn.pupperclient.management.mod.api.hud.design.impl;

import java.awt.Color;

import cn.pupperclient.management.mod.api.hud.design.HUDDesign;
import cn.pupperclient.skia.Skia;

import io.github.humbleui.skija.Font;

public class ClearDesign extends HUDDesign {

	public ClearDesign() {
		super("design.clear");
	}

	@Override
	public void drawBackground(float x, float y, float width, float height, float radius) {
		Skia.drawRoundedBlur(x, y, width, height, radius);
		Skia.drawShadow(x, y, width, height, radius);
	}

	@Override
	public void drawText(String text, float x, float y, Font font) {
		Skia.drawText(text, x, y, getTextColor(), font);
	}

	@Override
	public Color getTextColor() {
		return Color.WHITE;
	}

	@Override
	public Color getOnTextColor() {
		return Color.BLACK;
	}
}
