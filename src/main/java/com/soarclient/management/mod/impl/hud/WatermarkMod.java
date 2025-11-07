package com.soarclient.management.mod.impl.hud;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import com.soarclient.Soar;
import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.logger.SoarLogger;
import com.soarclient.management.color.api.ColorPalette;
import com.soarclient.management.mod.api.hud.HUDMod;
import com.soarclient.management.mod.settings.impl.BooleanSetting;
import com.soarclient.management.mod.settings.impl.StringSetting;
import com.soarclient.skia.Skia;
import com.soarclient.skia.font.Fonts;
import com.soarclient.skia.font.Icon;
import com.soarclient.utils.ColorUtils;

import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.skija.Image;
import io.github.humbleui.types.Rect;

public class WatermarkMod extends HUDMod {

    private static WatermarkMod instance;
    private Image logoImage;
    private long lastColorUpdate = 0;
    private Color currentColor = Color.WHITE;

    private static final int ISLAND_WIDTH = 200;
    private static final int ISLAND_HEIGHT = 40;
    private static final int ISLAND_CORNER_RADIUS = 20;
    private static int islandX, islandY;

    private static final int ICON_SIZE = 24;
    private static final int TEXT_SIZE = 14;
    private static final int PADDING = 10;

    private static final Color ISLAND_BG_COLOR = new Color(255, 255, 255, 15);
    private static final Color TEXT_COLOR = Color.BLACK;

    // Settings
    private final StringSetting textSetting = new StringSetting("setting.text",
        "setting.text.description", Icon.TEXT_FIELDS, this, "Soar Client");
    private final BooleanSetting showLogoSetting = new BooleanSetting("setting.showLogo",
        "setting.showLogo.description", Icon.IMAGE, this, true);

    public WatermarkMod() {
        super("mod.watermark.name", "mod.watermark.description", Icon.BRANDING_WATERMARK);
        instance = this;

        try (InputStream is = getClass().getResourceAsStream("/assets/pupper/logo.png")) {
            if (is != null) {
                byte[] imageData = is.readAllBytes();
                this.logoImage = Image.makeDeferredFromEncodedBytes(imageData);
            }
        } catch (IOException e) {
            SoarLogger.error("WatermarkMod", "Failed to load logo", e);
        }
    }

    public static WatermarkMod getInstance() {
        return instance;
    }

    public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
        this.draw();
    };

    public static void setIslandY(int islandY) {
        WatermarkMod.islandY = islandY;
    }

    public static void setIslandX(int islandX) {
        WatermarkMod.islandX = islandX;
    }

    private void draw() {
        try {
            this.begin();
            drawContent();
        } catch (Exception e) {
            SoarLogger.error("WatermarkMod", "Error in draw(): ", e);
            position.setSize(100, 20);
        } finally {
            try {
                this.finish();
            } catch (Exception e) {
                SoarLogger.error("WatermarkMod", "Error in finish(): ", e);
            }
        }
    }

    private void drawContent() {
        float padding = 5f;
        float currentX = getX() + padding;
        float contentHeight = 0;

        if (showLogoSetting.isEnabled() && logoImage != null) {
            float logoHeight = 28f;
            float logoWidth = logoHeight * (logoImage.getWidth() / (float) logoImage.getHeight());

            Skia.drawImage("logo.png", currentX, getY() + padding, logoWidth, logoHeight);
            currentX += logoWidth + padding;
            contentHeight = logoHeight;
        }

        // Draw text
        String text = textSetting.getValue();
        if (!text.isEmpty()) {
            float fontSize = 24f;
            Rect textBounds = Skia.getTextBounds(text, Fonts.getMedium(fontSize));
            FontMetrics metrics = Fonts.getMedium(fontSize).getMetrics();

            float textCenterY = (metrics.getAscent() - metrics.getDescent()) / 2 - metrics.getAscent();
            float textY = getY() + padding + (contentHeight / 2) - textCenterY;

            Color textColor = getSmoothAnimatedColor();
            Skia.drawText(text, currentX, textY, textColor, Fonts.getMedium(fontSize));

            contentHeight = fontSize;
            currentX += textBounds.getWidth() + padding;
        }

        position.setSize(currentX - getX() + padding, contentHeight + padding * 2);
    }


    private Color getSmoothAnimatedColor() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastColorUpdate < 16) { // ~60fps
            return currentColor;
        }
        lastColorUpdate = currentTime;

        try {
            ColorPalette palette = Soar.getInstance().getColorManager().getPalette();
            if (palette == null) {
                return Color.WHITE;
            }

            // Get colors from palette with fallbacks
            Color color1 = palette.getPrimary() != null ? palette.getPrimary() : new Color(240,255,255);
            Color color2 = palette.getSecondary() != null ? palette.getSecondary() : new Color(240,255,255);
            Color color3 = palette.getTertiary() != null ? palette.getTertiary() : new Color(240,255,255);

            double speed = 0.0001;
            double cycle = (currentTime * speed) % 1.0;

            double wave = Math.sin(cycle * Math.PI * 2);
            double normalizedWave = (wave + 1) / 2;

            if (normalizedWave < 0.33) {
                currentColor = ColorUtils.blend(color1, color2, (float) (normalizedWave * 3));
            } else if (normalizedWave < 0.66) {
                currentColor = ColorUtils.blend(color2, color3, (float) ((normalizedWave - 0.33) * 3));
            } else {
                currentColor = ColorUtils.blend(color3, color1, (float) ((normalizedWave - 0.66) * 3));
            }

            return currentColor;
        } catch (Exception e) {
            SoarLogger.error("WatermarkMod", "Error in getSmoothAnimatedColor(): ", e);
            return Color.WHITE;
        }
    }

    @Override
    public float getRadius() {
        return 4f;
    }
}
