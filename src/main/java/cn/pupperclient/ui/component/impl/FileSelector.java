package cn.pupperclient.ui.component.impl;

import java.io.File;

import org.lwjgl.glfw.GLFW;

import cn.pupperclient.Soar;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.ui.component.Component;
import cn.pupperclient.ui.component.api.PressAnimation;
import cn.pupperclient.ui.component.handler.impl.FileSelectorHandler;
import cn.pupperclient.utils.Multithreading;
import cn.pupperclient.utils.file.dialog.SoarFileDialog;
import cn.pupperclient.utils.language.I18n;
import cn.pupperclient.utils.mouse.MouseUtils;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

public class FileSelector extends Component {

	private PressAnimation pressAnimation = new PressAnimation();
	private String[] extensions;
	private File file;

	public FileSelector(float x, float y, File file, String[] extensions) {
		super(x, y);
		width = 126;
		height = 32;
		this.file = file;
		this.extensions = extensions;
	}

	@Override
	public void draw(double mouseX, double mouseY) {

		ColorPalette palette = Soar.getInstance().getColorManager().getPalette();

		Skia.drawRoundedRect(x, y, width, height, 12, palette.getPrimary());
		Skia.save();
		Skia.clip(x, y, width, height, 12);
		pressAnimation.draw(x, y, width, height, palette.getPrimaryContainer(), 0.12F);
		Skia.restore();

		String fileName = file != null ? file.getName() : "None";

		Skia.drawFullCenteredText(fileName, x + (width / 2), y + (height / 2), palette.getSurface(),
				Fonts.getMedium(14));
	}

	@Override
	public void mousePressed(double mouseX, double mouseY, int button) {
		if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			pressAnimation.onPressed(mouseX, mouseY, x, y);
			Multithreading.runAsync(() -> {

				ObjectObjectImmutablePair<Boolean, File> p = SoarFileDialog.chooseFile(I18n.get("text.selectfile"), extensions);

				if (p.left()) {
					file = p.right();
					if (handler instanceof FileSelectorHandler) {
						((FileSelectorHandler) handler).onSelect(file);
					}
				}
			});
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		pressAnimation.onReleased(mouseX, mouseY, x, y);
	}
}
