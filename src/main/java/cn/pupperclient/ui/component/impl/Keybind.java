package cn.pupperclient.ui.component.impl;

import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import cn.pupperclient.PupperClient;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.ui.component.Component;
import cn.pupperclient.ui.component.api.PressAnimation;
import cn.pupperclient.ui.component.handler.impl.KeybindHandler;
import cn.pupperclient.utils.mouse.MouseUtils;
import com.mojang.blaze3d.platform.InputConstants;

public class Keybind extends Component {

	private PressAnimation pressAnimation = new PressAnimation();

	private boolean binding;
	private InputConstants.Key key;

	public Keybind(float x, float y, InputConstants.Key key) {
		super(x, y);
		this.key = key;
		width = 126;
		height = 32;
	}

	@Override
	public void draw(double mouseX, double mouseY) {

		ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

		Skia.drawRoundedRect(x, y, width, height, 12, palette.getPrimary());
		Skia.save();
		Skia.clip(x, y, width, height, 12);
		pressAnimation.draw(x, y, width, height, palette.getPrimaryContainer(), 0.12F);
		Skia.restore();

		Skia.drawFullCenteredText(binding ? "..." : key.getDisplayName().getString(), x + (width / 2),
				y + (height / 2), palette.getSurface(), Fonts.getMedium(14));
	}

	@Override
	public void mousePressed(double mouseX, double mouseY, int button) {
		if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			pressAnimation.onPressed(mouseX, mouseY, x, y);
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {

		if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && !binding) {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
				binding = true;
			}
			return;
		}

		if (binding) {

			if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
				setKeyCode(InputConstants.UNKNOWN);
			} else if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT
					&& button != GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
				setKeyCode(InputConstants.Type.MOUSE.getOrCreate(button));
			}

			binding = false;
		}

		pressAnimation.onReleased(mouseX, mouseY, x, y);
	}

	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if (binding) {
			setKeyCode(InputConstants.getKey(new KeyEvent(keyCode, scanCode, modifiers)));
			this.binding = false;
		}
	}

	public InputConstants.Key getKeyCode() {
		return key;
	}

	public void setKeyCode(InputConstants.Key key) {

		this.key = key;

		if (handler instanceof KeybindHandler) {
			((KeybindHandler) handler).onBinded(key);
		}
	}
}
