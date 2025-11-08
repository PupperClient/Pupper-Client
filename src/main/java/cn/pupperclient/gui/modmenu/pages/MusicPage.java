package cn.pupperclient.gui.modmenu.pages;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pupperclient.PupperClient;
import org.lwjgl.glfw.GLFW;

import cn.pupperclient.animation.SimpleAnimation;
import cn.pupperclient.gui.api.SoarGui;
import cn.pupperclient.gui.api.page.Page;
import cn.pupperclient.gui.api.page.impl.RightLeftTransition;
import cn.pupperclient.gui.modmenu.component.MusicControlBar;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.management.music.Music;
import cn.pupperclient.management.music.MusicManager;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;
import cn.pupperclient.utils.ColorUtils;
import cn.pupperclient.utils.SearchUtils;
import cn.pupperclient.utils.mouse.MouseUtils;

import io.github.humbleui.skija.ClipMode;
import io.github.humbleui.skija.FilterTileMode;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.ImageFilter;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Path;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;

public class MusicPage extends Page {

	private SimpleAnimation controlBarAnimation = new SimpleAnimation();
	private MusicControlBar controlBar;
	private List<Item> items = new ArrayList<>();

	public MusicPage(SoarGui parent) {
		super(parent, "text.music", Icon.MUSIC_NOTE, new RightLeftTransition(true));
	}

	@Override
	public void init() {
		super.init();
		
		items.clear();
		
		for (Music m : PupperClient.getInstance().getMusicManager().getMusics()) {
			items.add(new Item(m));
		}

		controlBar = new MusicControlBar(x + 22, y + height - 60 - 18, width - 44);

		for (Item i : items) {
			i.xAnimation.setFirstTick(true);
			i.yAnimation.setFirstTick(true);
		}
	}

	@Override
	public void draw(double mouseX, double mouseY) {

		super.draw(mouseX, mouseY);

		MusicManager musicManager = PupperClient.getInstance().getMusicManager();
		ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

		int index = 0;
		float offsetX = 28;
		float offsetY = 96;

		controlBarAnimation.onTick(MouseUtils.isInside(mouseX, mouseY, controlBar.getX(), controlBar.getY(),
				controlBar.getWidth(), controlBar.getHeight()) ? 1 : 0, 12);

		mouseY = mouseY - scrollHelper.getValue();

		Skia.save();
		Skia.translate(0, scrollHelper.getValue());

		for (Item i : items) {

			Music m = i.music;
			SimpleAnimation xAnimation = i.xAnimation;
			SimpleAnimation yAnimation = i.yAnimation;
			SimpleAnimation focusAnimation = i.focusAnimation;

			if (!searchBar.getText().isEmpty()
					&& !SearchUtils.isSimilar(m.getTitle() + " " + m.getArtist(), searchBar.getText())) {
				continue;
			}

			float itemX = x + offsetX;
			float itemY = y + offsetY;

			xAnimation.onTick(itemX, 14);
			yAnimation.onTick(itemY, 14);
			focusAnimation.onTick(MouseUtils.isInside(mouseX, mouseY, itemX, itemY, 174, 174) ? 1 : 0, 10);

			itemX = xAnimation.getValue();
			itemY = yAnimation.getValue();

			if (m.getAlbum() != null) {
				drawRoundedImage(m.getAlbum(), itemX, itemY, 174, 174, 26,
						(Math.abs(focusAnimation.getValue()) + 0.001F) * 6);
			} else {
				Skia.drawRoundedRect(itemX, itemY, 174, 174, 26, palette.getSurfaceContainerHigh());
			}

			String limitedTitle = Skia.getLimitText(m.getTitle(), Fonts.getRegular(15), 174);
			String limitedArtist = Skia.getLimitText(m.getArtist(), Fonts.getRegular(12), 174);

			Skia.drawText(limitedTitle, itemX, itemY + 174 + 6, palette.getOnSurface(), Fonts.getRegular(15));
			Skia.drawText(limitedArtist, itemX, itemY + 174 + 6 + 15, palette.getOnSurfaceVariant(),
					Fonts.getRegular(12));

			String icon = musicManager.getCurrentMusic() != null && musicManager.getCurrentMusic().equals(m)
					&& musicManager.isPlaying() ? Icon.PAUSE : Icon.PLAY_ARROW;

			Skia.save();
			Skia.translate(0, 15 - (focusAnimation.getValue() * 15));
			Skia.drawFullCenteredText(icon, itemX + (174 / 2), itemY + (174 / 2),
					ColorUtils.applyAlpha(Color.WHITE, focusAnimation.getValue()), Fonts.getIconFill(64));
			Skia.restore();

			offsetX += 174 + 32;
			index++;

			if (index % 4 == 0) {
				offsetX = 28;
				offsetY += 206 + 23;
			}
		}

		scrollHelper.setMaxScroll(206, 23, index, 4, height - 96);
		Skia.restore();

		mouseY = mouseY + scrollHelper.getValue();

		Skia.save();
		Skia.translate(0, 100 - (controlBarAnimation.getValue() * 100));
		controlBar.draw(mouseX, mouseY);
		Skia.restore();
	}

	@Override
	public void mousePressed(double mouseX, double mouseY, int button) {
		super.mousePressed(mouseX, mouseY, button);

		controlBar.mousePressed(mouseX, mouseY, button);

		if (MouseUtils.isInside(mouseX, mouseY, controlBar.getX(), controlBar.getY(), controlBar.getWidth(),
				controlBar.getHeight())) {
			return;
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		super.mouseReleased(mouseX, mouseY, button);

		MusicManager musicManager = PupperClient.getInstance().getMusicManager();

		controlBar.mouseReleased(mouseX, mouseY, button);

		if (MouseUtils.isInside(mouseX, mouseY, controlBar.getX(), controlBar.getY(), controlBar.getWidth(),
				controlBar.getHeight())) {
			return;
		}

		mouseY = mouseY - scrollHelper.getValue();

		for (Item i : items) {

			Music m = i.music;
			float itemX = i.xAnimation.getValue();
			float itemY = i.yAnimation.getValue();

			if (!searchBar.getText().isEmpty()
					&& !SearchUtils.isSimilar(m.getTitle() + " " + m.getArtist(), searchBar.getText())) {
				continue;
			}

			if (MouseUtils.isInside(mouseX, mouseY, itemX, itemY, 174, 174) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

				if (musicManager.getCurrentMusic() != m) {
					musicManager.stop();
					musicManager.setCurrentMusic(m);
					musicManager.play();
				} else {
					musicManager.switchPlayBack();
				}
			}
		}
	}
	
	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		super.keyPressed(keyCode, scanCode, modifiers);
		controlBar.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public void charTyped(char chr, int modifiers) {
		super.charTyped(chr, modifiers);
		controlBar.charTyped(chr, modifiers);
	}

	private void drawRoundedImage(File file, float x, float y, float width, float height, float cornerRadius,
			float blurRadius) {

		Path path = new Path();
		path.addRRect(RRect.makeXYWH(x, y, width, height, cornerRadius));

		Paint blurPaint = new Paint();
		blurPaint.setImageFilter(ImageFilter.makeBlur(blurRadius, blurRadius, FilterTileMode.CLAMP));

		Skia.save();

		Skia.getCanvas().clipPath(path, ClipMode.INTERSECT, true);

		Skia.drawImage(file, x, y, width, height);

		if (Skia.getImageHelper().load(file)) {
			Image image = Skia.getImageHelper().get(file.getName());
			if (image != null) {
				Skia.getCanvas().drawImageRect(image, Rect.makeWH(image.getWidth(), image.getHeight()),
						Rect.makeXYWH(x, y, width, height), blurPaint, true);
			}
		}

		Skia.restore();
	}

	private class Item {

		private Music music;
		private SimpleAnimation xAnimation = new SimpleAnimation();
		private SimpleAnimation yAnimation = new SimpleAnimation();
		private SimpleAnimation focusAnimation = new SimpleAnimation();

		private Item(Music music) {
			this.music = music;
		}
	}
}
