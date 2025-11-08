package cn.pupperclient.ui.component.impl;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import cn.pupperclient.Soar;
import cn.pupperclient.animation.Animation;
import cn.pupperclient.animation.Duration;
import cn.pupperclient.animation.cubicbezier.impl.EaseStandard;
import cn.pupperclient.animation.other.DummyAnimation;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.ui.component.Component;
import cn.pupperclient.ui.component.api.PressAnimation;
import cn.pupperclient.ui.component.handler.impl.ComboButtonHandler;
import cn.pupperclient.utils.ColorUtils;
import cn.pupperclient.utils.language.I18n;
import cn.pupperclient.utils.mouse.MouseUtils;

public class ComboButton extends Component {

	private PressAnimation pressAnimation = new PressAnimation();

	private Animation animation;
	private List<String> options;
	private String option;

	public ComboButton(float x, float y, List<String> options, String option) {
		super(x, y);
		this.options = options;
		this.option = option;
		this.animation = new DummyAnimation(1);
		width = 126;
		height = 32;
	}

	@Override
	public void draw(double mouseX, double mouseY) {

		ColorPalette palette = Soar.getInstance().getColorManager().getPalette();

		Skia.drawRoundedRect(x, y, width, height, 12, palette.getPrimary());
		Skia.save();
		Skia.clip(x, y, width, height, 12);
		pressAnimation.draw(x, y, width, height, palette.getPrimaryContainer(), 0.12F);
		Skia.restore();
		Skia.drawHeightCenteredText("<", x + 6, y + (height / 2), palette.getSurface(), Fonts.getMedium(14));
		Skia.drawHeightCenteredText(">", x + width - 16, y + (height / 2), palette.getSurface(), Fonts.getMedium(14));
		Skia.drawFullCenteredText(I18n.get(option),
				x + (width / 2) + ((animation.getEnd() - animation.getValue()) * 22), y + (height / 2),
				ColorUtils.applyAlpha(palette.getSurface(), Math.abs(animation.getValue())), Fonts.getMedium(14));
	}

	@Override
	public void mousePressed(double mouseX, double mouseY, int button) {
		if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			pressAnimation.onPressed(mouseX, mouseY, x, y);
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {

		int max = options.size();
		int index = options.indexOf(option);

		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

			if (MouseUtils.isInside(mouseX, mouseY, x, y, 32, 32)) {

				animation = new EaseStandard(Duration.MEDIUM_3, 0, 1);

				if (index > 0) {
					index--;
				} else {
					index = max - 1;
				}

				setOption(options.get(index));
			}

			if (MouseUtils.isInside(mouseX, mouseY, x + width - 32, y, 32, 32)) {

				animation = new EaseStandard(Duration.MEDIUM_3, 0, -1);

				if (index < max - 1) {
					index++;
				} else {
					index = 0;
				}

				setOption(options.get(index));
			}
		}

		pressAnimation.onReleased(mouseX, mouseY, x, y);
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {

		this.option = option;

		if (handler instanceof ComboButtonHandler) {
			((ComboButtonHandler) handler).onChanged(option);
		}
	}
}
