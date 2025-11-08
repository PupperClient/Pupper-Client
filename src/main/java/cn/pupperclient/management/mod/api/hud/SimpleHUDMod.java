package cn.pupperclient.management.mod.api.hud;

import cn.pupperclient.management.mod.settings.impl.BooleanSetting;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;

import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.types.Rect;

public abstract class SimpleHUDMod extends HUDMod {

	protected BooleanSetting iconSetting = new BooleanSetting("setting.icon", "setting.icon.description",
			Icon.NEW_RELEASES, this, true);

	public SimpleHUDMod(String name, String description, String icon) {
		super(name, description, icon);
	}

	protected void draw() {

		float fontSize = 9;
		float iconSize = 10.5F;
		float padding = 5;
		boolean hasIcon = getIcon() != null && iconSetting.isEnabled();
		Rect textBounds = Skia.getTextBounds(getText(), Fonts.getRegular(fontSize));
		Rect iconBounds = Skia.getTextBounds(getIcon(), Fonts.getIcon(iconSize));
		FontMetrics metrics = Fonts.getRegular(fontSize).getMetrics();
		float width = textBounds.getWidth() + (padding * 2) + (hasIcon ? iconBounds.getWidth() + 4 : 0);
		float height = fontSize + (padding * 2) - 1.5F;
		float textCenterY = (metrics.getAscent() - metrics.getDescent()) / 2 - metrics.getAscent();

		this.begin();
		this.drawBackground(getX(), getY(), width, height);

		if (hasIcon) {
			this.drawText(getIcon(), getX() + padding, getY() + (height / 2) - (iconBounds.getHeight() / 2),
					Fonts.getIcon(iconSize));
		}

		this.drawText(getText(), getX() + padding + (hasIcon ? iconBounds.getWidth() + 4 : 0),
				getY() + (height / 2) - textCenterY, Fonts.getRegular(fontSize));
		this.finish();

		position.setSize(width, height);
	}

    @Override
	public float getRadius() {
		return 6;
	}

	public abstract String getText();

	public abstract String getIcon();
}
