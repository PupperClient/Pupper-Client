package com.soarclient.management.mod.api.hud;

import com.soarclient.Soar;
import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.management.mod.api.Position;
import com.soarclient.management.mod.api.hud.design.HUDDesign;
import com.soarclient.skia.Skia;

import io.github.humbleui.skija.Font;

public abstract class HUDMod extends Mod {

	protected Position position;

	public HUDMod(String name, String description, String icon) {
		super(name, description, icon, ModCategory.HUD);
		this.position = new Position(100, 100, 0, 0);
		this.setMovable(true);
	}

	public void begin() {
		Skia.save();
		Skia.scale(position.getX(), position.getY(), position.getScale());
	}

	public void finish() {
		Skia.restore();
	}

	public void drawBackground(float x, float y, float width, float height) {
		getDesign().drawBackground(x, y, width, height, getRadius());
	}
	
	public void drawBlurBackground(float x, float y, float width, float height) {
		Skia.drawRoundedBlur(x, y, width, height, getRadius());
		Skia.drawShadow(x, y, width, height, getRadius());
	}

	public void drawText(String text, float x, float y, Font font) {
		getDesign().drawText(text, x, y, font);
	}

	public abstract float getRadius();

	public HUDDesign getDesign() {
		return Soar.getInstance().getModManager().getCurrentDesign();
	}

	public Position getPosition() {
		return position;
	}

	public float getX() {
		return position.getX();
	}

	public float getY() {
		return position.getY();
	}
}
