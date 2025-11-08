package cn.pupperclient.gui.modmenu.pages;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.pupperclient.PupperClient;
import cn.pupperclient.gui.api.SoarGui;
import cn.pupperclient.gui.api.page.Page;
import cn.pupperclient.gui.api.page.impl.RightLeftTransition;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;

public class HomePage extends Page {
    private String currentTime;

    public HomePage(SoarGui parent) {
        super(parent, "text.home", Icon.HOME, new RightLeftTransition(true));
    }

    @Override
    public void init() {
        super.init();

        int buttonSpacing = 20;
        float buttonY = y + height - 100;

        updateTime();
    }

    @Override
    public void draw(double mouseX, double mouseY) {
        super.draw(mouseX, mouseY);
        updateTime();

        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

        Skia.drawGradientRoundedRect(x, y, width, height, 20,
            palette.getSurfaceContainerLow(),
            palette.getSurfaceContainer());

        Skia.drawText("Welcome to", x + 50, y + 80, palette.getOnSurfaceVariant(), Fonts.getMedium(24));
        Skia.drawText("PupperClient-CN " + PupperClient.getInstance().getVersion(), x + 50, y + 110, palette.getOnSurface(), Fonts.getRegular(36));

        Skia.drawText(currentTime, x + width - 325, y + 80, palette.getOnSurfaceVariant(),
            Fonts.getMedium(24));

        drawFeatureCards(palette);
    }


    private void drawFeatureCards(ColorPalette palette) {
        float cardWidth = (width - 150) / 3;
        float cardHeight = 160;
        float cardY = y + 330;

        drawFeatureCard(x + 50, cardY, cardWidth, cardHeight,
            Icon.INVENTORY_2, "Mod Manager",
            "Manage your soar settings",
            palette.getPrimaryContainer(), palette);

        drawFeatureCard(x + 75 + cardWidth, cardY, cardWidth, cardHeight,
            Icon.DESCRIPTION, "Profiles",
            "Switch soar profiles",
            palette.getSecondaryContainer(), palette);

        drawFeatureCard(x + 100 + 2 * cardWidth, cardY, cardWidth, cardHeight,
            Icon.MUSIC_NOTE, "Music Player",
            "Enjoy your favorite music",
            palette.getTertiaryContainer(), palette);
    }

    private void drawFeatureCard(float x, float y, float width, float height,
                                 String icon, String title, String description,
                                 Color bgColor, ColorPalette palette) {
        Skia.drawRoundedRect(x, y, width, height, 20, bgColor);

        float iconSize = 48;
        Skia.drawFullCenteredText(icon, x + width / 2, y + 40,
            palette.getOnSurface(), Fonts.getIcon(iconSize));

        Skia.drawFullCenteredText(title, x + width / 2, y + 100,
            palette.getOnSurface(), Fonts.getMedium(20));

        Skia.drawFullCenteredText(description, x + width / 2, y + 130,
            palette.getOnSurfaceVariant(), Fonts.getRegular(14));
    }

    private void updateTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");

        Date now = new Date();
        currentTime = timeFormat.format(now) + " | " + dateFormat.format(now);
    }

    @Override
    public void onClosed() {
        this.setTransition(new RightLeftTransition(false));
    }
}
