package com.soarclient.gui.modmenu.pages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.soarclient.Soar;
import com.soarclient.animation.SimpleAnimation;
import com.soarclient.gui.api.SoarGui;
import com.soarclient.gui.api.page.Page;
import com.soarclient.gui.api.page.impl.LeftRightTransition;
import com.soarclient.gui.api.page.impl.RightLeftTransition;
import com.soarclient.gui.modmenu.pages.profile.ProfileAddPage;
import com.soarclient.management.color.api.ColorPalette;
import com.soarclient.management.profile.Profile;
import com.soarclient.management.profile.ProfileIcon;
import com.soarclient.skia.Skia;
import com.soarclient.skia.font.Fonts;
import com.soarclient.skia.font.Icon;
import com.soarclient.ui.component.handler.impl.ButtonHandler;
import com.soarclient.ui.component.impl.IconButton;
import com.soarclient.utils.SearchUtils;
import com.soarclient.utils.mouse.MouseUtils;

public class ProfilePage extends Page {

	private final List<Item> items = new ArrayList<>();
	private IconButton addButton;

	public ProfilePage(SoarGui parent) {
		super(parent, "text.profile", Icon.DESCRIPTION, new RightLeftTransition(true));
		addButton = new IconButton(Icon.ADD, 0, 0, IconButton.Size.LARGE, IconButton.Style.SECONDARY);
	}

	@Override
	public void init() {
		super.init();
		
		items.clear();
		
		for (Profile p : Soar.getInstance().getProfileManager().getProfiles()) {
			items.add(new Item(p));
		}

		for (Item i : items) {
			i.xAnimation.setFirstTick(true);
			i.yAnimation.setFirstTick(true);
		}

		addButton = new IconButton(Icon.ADD, x + width - addButton.getWidth() - 20,
				y + height - addButton.getHeight() - 20, IconButton.Size.LARGE, IconButton.Style.SECONDARY);
		addButton.setHandler(new ButtonHandler() {
			@Override
			public void onAction() {
				parent.setCurrentPage(new ProfileAddPage(parent, ProfilePage.this.getClass()));
				ProfilePage.this.setTransition(new LeftRightTransition(true));
			}
		});
	}

	@Override
	public void draw(double mouseX, double mouseY) {
		super.draw(mouseX, mouseY);

		ColorPalette palette = Soar.getInstance().getColorManager().getPalette();

		addButton.draw(mouseX, mouseY);

		mouseY = mouseY - scrollHelper.getValue();

		int index = 0;
		float offsetX = 26;
		float offsetY = 0;

		Skia.save();
		Skia.translate(0, scrollHelper.getValue());

		for (Item i : items) {

			Profile p = i.profile;
			SimpleAnimation xAnimation = i.xAnimation;
			SimpleAnimation yAnimation = i.yAnimation;
			Object icon = p.getIcon();

			float itemX = x + offsetX;
			float itemY = y + 96 + offsetY;

			if (!searchBar.getText().isEmpty()
					&& !SearchUtils.isSimilar(p.getName() + " " + p.getAuthor(), searchBar.getText())) {
				continue;
			}

			xAnimation.onTick(itemX, 14);
			yAnimation.onTick(itemY, 14);

			itemX = xAnimation.getValue();
			itemY = yAnimation.getValue();

			Skia.drawRoundedRect(itemX, itemY, 245, 88, 12, palette.getSurface());
			
			if (icon instanceof ProfileIcon) {
				Skia.drawRoundedImage(((ProfileIcon) icon).getIconPath(), itemX + 8, itemY + 8, 72, 72, 12);
			} else if (icon instanceof File) {
				Skia.drawRoundedImage(((File) icon), itemX + 8, itemY + 8, 72, 72, 12);
			} else {
				Skia.drawRoundedRect(itemX + 8, itemY + 8, 72, 72, 12, palette.getSurfaceContainer());
			}
			
			Skia.drawText(p.getName(), itemX + 86, itemY + 16, palette.getOnSurface(), Fonts.getMedium(20));

			index++;
			offsetX += 26 + 245;

			if (index % 3 == 0) {
				offsetX = 26;
				offsetY += 22 + 88;
			}
		}
		
		scrollHelper.setMaxScroll(88, 22, index, 3, height - 96);

		Skia.restore();
	}

	@Override
	public void mousePressed(double mouseX, double mouseY, int button) {
		super.mousePressed(mouseX, mouseY, button);
		addButton.mousePressed(mouseX, mouseY, button);
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		super.mouseReleased(mouseX, mouseY, button);
		addButton.mouseReleased(mouseX, mouseY, button);
		
		for (Item i : items) {
			
			float itemX = i.xAnimation.getValue();
			float itemY = i.yAnimation.getValue();
			
			if(MouseUtils.isInside(mouseX, mouseY, itemX, itemY, 245, 88) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
				Soar.getInstance().getProfileManager().load(i.profile);
			}
		}
	}

	@Override
	public void onClosed() {
		this.setTransition(new RightLeftTransition(true));
	}

	private class Item {

		private final Profile profile;
		private final SimpleAnimation xAnimation = new SimpleAnimation();
		private final SimpleAnimation yAnimation = new SimpleAnimation();

		private Item(Profile profile) {
			this.profile = profile;
		}
	}
}
