package com.soarclient.gui;

import com.soarclient.Soar;
import com.soarclient.gui.api.SimpleSoarGui;
import com.soarclient.management.color.api.ColorPalette;
import com.soarclient.skia.Skia;
import com.soarclient.skia.font.Icon;
import com.soarclient.ui.component.impl.IconButton;
import com.soarclient.ui.component.handler.impl.ButtonHandler;
import com.soarclient.utils.ColorUtils;
import com.soarclient.utils.language.I18n;
import com.soarclient.utils.mouse.MouseUtils;
import com.soarclient.utils.mouse.ScrollHelper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.awt.Color;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Typeface;
import io.github.humbleui.types.RRect;

public class VersionSelectGui extends SimpleSoarGui {

    private final MultiplayerGui parent;
    private final List<VersionItem> versionItems = new ArrayList<>();
    private IconButton backButton;
    private final ScrollHelper scrollHelper = new ScrollHelper();
    private float lastWindowWidth = 0;
    private float lastWindowHeight = 0;
    private boolean wasMinimized = false;
    private float parallaxX = 0;
    private float parallaxY = 0;
    private float listHeight = 0;

    // Skija resources
    private Paint backgroundPaint;
    private Paint surfaceContainerPaint;
    private Paint surfacePaint;
    private Paint primaryPaint;
    private Paint surfaceVariantPaint;
    private Paint onSurfaceVariantPaint;
    private Font regularFont;

    public VersionSelectGui(MultiplayerGui parent) {
        super(false);
        this.parent = parent;
        initializeSkijaResources();
    }

    private void initializeSkijaResources() {
        backgroundPaint = new Paint();
        surfaceContainerPaint = new Paint();
        surfacePaint = new Paint();
        primaryPaint = new Paint();
        surfaceVariantPaint = new Paint();
        onSurfaceVariantPaint = new Paint();
        try {
            Typeface typeface = Typeface.makeDefault();
            regularFont = new Font(typeface, 16f);
        } catch (Exception e) {
            Soar.LOGGER.error("Failed to initialize fonts", e);
        }
    }

    @Override
    public void init() {
        updateLayout();
    }

    private void updateLayout() {
        versionItems.clear();

        float scaleFactor = calculateScaleFactor();
        float centerX = client.getWindow().getWidth() / 2f;

        float listWidth = Math.min(500 * scaleFactor, client.getWindow().getWidth() - 80 * scaleFactor);
        listHeight = Math.min(400 * scaleFactor, client.getWindow().getHeight() - 200 * scaleFactor) + 1125;

        float listX = centerX - listWidth / 2;
        float listY = 100 * scaleFactor;

        loadVersionList(listX, listY, listWidth, listHeight, scaleFactor);

        float topButtonSize = 45 * scaleFactor;

        backButton = new IconButton(Icon.ARROW_BACK,
            client.getWindow().getWidth() - topButtonSize - 20 * scaleFactor,
            25 * scaleFactor,
            IconButton.Size.NORMAL,
            IconButton.Style.TERTIARY);
        backButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                client.setScreen(parent.build());
            }
        });

        lastWindowWidth = client.getWindow().getWidth();
        lastWindowHeight = client.getWindow().getHeight();
    }

    private void loadVersionList(float x, float y, float width, float height, float scaleFactor) {
        float newItemHeight = 50 * scaleFactor;

        List<ProtocolVersion> versions = new ArrayList<>(ProtocolVersion.getProtocols());
        versions.sort(Comparator.comparingInt(ProtocolVersion::getVersion).reversed());

        for (int i = 0; i < versions.size(); i++) {
            ProtocolVersion version = versions.get(i);
            float itemY = y + i * (newItemHeight + 5 * scaleFactor);
            versionItems.add(new VersionItem(version, x, itemY, width, newItemHeight, scaleFactor));
        }

        float totalHeight = versions.size() * (newItemHeight + 5 * scaleFactor);
        scrollHelper.setMaxScroll(totalHeight, height);
    }

    private boolean isWindowMinimized() {
        return client.getWindow().getWidth() < 100 || client.getWindow().getHeight() < 100;
    }

    private float calculateScaleFactor() {
        float currentWidth = client.getWindow().getWidth();
        float currentHeight = client.getWindow().getHeight();

        if (isWindowMinimized()) {
            return 0.5f;
        }

        float windowArea = currentWidth * currentHeight;

        if (windowArea < 800 * 600) {
            return 1.4f;
        } else if (windowArea < 1280 * 720) {
            return 1.2f;
        } else if (windowArea < 1920 * 1080) {
            return 1.0f;
        } else {
            return 0.9f;
        }
    }

    @Override
    public void draw(double mouseX, double mouseY) {
        boolean currentlyMinimized = isWindowMinimized();

        if (client.getWindow().getWidth() != lastWindowWidth ||
            client.getWindow().getHeight() != lastWindowHeight ||
            wasMinimized != currentlyMinimized) {
            updateLayout();
            wasMinimized = currentlyMinimized;
        }

        if (currentlyMinimized) {
            return;
        }

        Soar instance = Soar.getInstance();
        ColorPalette palette = instance.getColorManager().getPalette();

        // 更新paint颜色
        updatePaintColors(palette);

        // 绘制背景
        drawCustomBackground();

        // 绘制标题
        drawTitle(palette);

        // 绘制列表背景和内容
        drawListBackgroundAndItems(mouseX, mouseY);

        // 绘制按钮
        backButton.draw(mouseX, mouseY);
    }

    private void updatePaintColors(ColorPalette palette) {
        surfaceContainerPaint.setColor(palette.getSurfaceContainer().getRGB());
        surfacePaint.setColor(palette.getSurface().getRGB());
        primaryPaint.setColor(palette.getPrimary().getRGB());
        surfaceVariantPaint.setColor(palette.getSurfaceVariant().getRGB());
        onSurfaceVariantPaint.setColor(palette.getOnSurfaceVariant().getRGB());
    }

    /**
     * 绘制标题
     */
    private void drawTitle(ColorPalette palette) {
        float scaleFactor = calculateScaleFactor();
        String title = I18n.get("menu.version_select");

        // 计算文本宽度以居中显示
        float titleFontSize = 28 * scaleFactor;
        Font titleFont = new Font(regularFont.getTypeface(), titleFontSize);
        float titleWidth = titleFont.measureTextWidth(title);

        float centerX = client.getWindow().getWidth() / 2f;
        float titleY = 60 * scaleFactor - 10;

        try (Paint titlePaint = new Paint().setColor(palette.getOnSurface().getRGB())) {
            Canvas canvas = getCanvas();
            canvas.drawString(title, centerX - titleWidth / 2, titleY, titleFont, titlePaint);
        }
    }

    /**
     * 绘制列表背景和项
     */
    private void drawListBackgroundAndItems(double mouseX, double mouseY) {
        float scaleFactor = calculateScaleFactor();
        float centerX = client.getWindow().getWidth() / 2f;
        float listWidth = Math.min(500 * scaleFactor, client.getWindow().getWidth() - 80 * scaleFactor);
        float listX = centerX - listWidth / 2;
        float listY = 100 * scaleFactor;
        float radius = 15 * scaleFactor;

        // 绘制列表背景
        drawRoundedRect(listX, listY, listWidth, Math.min(400 * scaleFactor, client.getWindow().getHeight() - 200 * scaleFactor) + 1125, radius, surfaceContainerPaint);

        // 设置裁剪区域
        Canvas canvas = getCanvas();
        canvas.save();
        try {
            // 创建圆角矩形裁剪路径
            RRect clipRect = RRect.makeXYWH(listX, listY, listWidth, listHeight, radius);
            canvas.clipRRect(clipRect);

            // 应用滚动偏移
            canvas.translate(0, scrollHelper.getValue());

            // 绘制版本项
            for (VersionItem versionItem : versionItems) {
                versionItem.draw((int) mouseX, (int) (mouseY - scrollHelper.getValue()));
            }
        } finally {
            canvas.restore();
        }
    }

    /**
     * 绘制圆角矩形（Skija实现）
     */
    private void drawRoundedRect(float x, float y, float width, float height, float radius, Paint paint) {
        Canvas canvas = getCanvas();
        RRect rect = RRect.makeXYWH(x, y, width, height, radius);
        canvas.drawRRect(rect, paint);
    }

    @Override
    public void mousePressed(double mouseX, double mouseY, int button) {
        if (isWindowMinimized()) {
            return;
        }

        double adjustedMouseY = mouseY - scrollHelper.getValue();

        for (VersionItem versionItem : versionItems) {
            versionItem.mousePressed((int) mouseX, (int) adjustedMouseY, button);
        }

        backButton.mousePressed(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (isWindowMinimized()) {
            return;
        }

        double adjustedMouseY = mouseY - scrollHelper.getValue();

        for (VersionItem versionItem : versionItems) {
            versionItem.mouseReleased((int) mouseX, (int) adjustedMouseY, button);
        }

        backButton.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 检查鼠标是否在列表区域内
        float centerX = client.getWindow().getWidth() / 2f;
        float listWidth = Math.min(500 * calculateScaleFactor(), client.getWindow().getWidth() - 80 * calculateScaleFactor());
        float listX = centerX - listWidth / 2;
        float listY = 100 * calculateScaleFactor();

        if (MouseUtils.isInside((float) mouseX, (float) mouseY, listX, listY, listWidth, listHeight)) {
            scrollHelper.onScroll(verticalAmount * 20); // 增加滚动速度
        }
    }

    private void cleanupSkijaResources() {
        if (backgroundPaint != null) backgroundPaint.close();
        if (surfaceContainerPaint != null) surfaceContainerPaint.close();
        if (surfacePaint != null) surfacePaint.close();
        if (primaryPaint != null) primaryPaint.close();
        if (surfaceVariantPaint != null) surfaceVariantPaint.close();
        if (onSurfaceVariantPaint != null) onSurfaceVariantPaint.close();
        if (regularFont != null) regularFont.close();
    }

    private Canvas getCanvas() {
        return Skia.getCanvas();
    }

    private class VersionItem {
        private final ProtocolVersion version;
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final float scaleFactor;
        private long lastClickTime = 0;
        private boolean isPressed = false;

        // 项级别的paint对象
        private final io.github.humbleui.skija.Paint itemPaint = new Paint();

        public VersionItem(ProtocolVersion version, float x, float y, float width, float height, float scaleFactor) {
            this.version = version;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.scaleFactor = scaleFactor;
        }

        public void draw(int mouseX, int mouseY) {
            ColorPalette palette = Soar.getInstance().getColorManager().getPalette();
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height);
            boolean isCurrentVersion = parent.getProtocolVersion().equals(version);

            float radius = 8 * scaleFactor;

            // 绘制背景
            itemPaint.setColor(palette.getSurface().getRGB());
            drawRoundedRect(x, y, width, height, radius, itemPaint);

            // 绘制悬停或选中效果
            if (hovered || isCurrentVersion) {
                Color highlightColor = isCurrentVersion ? palette.getPrimary() : palette.getSurfaceVariant();
                itemPaint.setColor(ColorUtils.applyAlpha(highlightColor, 0.2f).getRGB());
                drawRoundedRect(x, y, width, height, radius, itemPaint);
            }

            float fontSize = 16 * scaleFactor;
            float padding = 10 * scaleFactor;

            try (Font itemFont = new Font(regularFont.getTypeface(), fontSize);
                 Paint textPaint = new Paint()) {

                textPaint.setColor(isCurrentVersion ? palette.getPrimary().getRGB() : palette.getOnSurfaceVariant().getRGB());

                // 绘制版本名称
                String versionName = version.getName();
                Canvas canvas = getCanvas();
                canvas.drawString(versionName, x + padding, y + height / 2 + fontSize / 3, itemFont, textPaint);

                // 如果是当前版本，显示选中标记
                if (isCurrentVersion) {
                    String selectedText = "✓";
                    float selectedWidth = itemFont.measureTextWidth(selectedText);
                    canvas.drawString(selectedText,
                        x + width - padding - selectedWidth,
                        y + height / 2 + fontSize / 3,
                        itemFont, textPaint);
                }
            }
        }

        public void mousePressed(int mouseX, int mouseY, int mouseButton) {
            if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && mouseButton == 0) {
                isPressed = true;
            }
        }

        public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
            if (isPressed && MouseUtils.isInside(mouseX, mouseY, x, y, width, height) && mouseButton == 0) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < 300) { // 双击检测
                    parent.setProtocolVersion(version);
                    client.setScreen(parent.build());
                }
                lastClickTime = currentTime;
            }
            isPressed = false;
        }
    }

    private void drawCustomBackground() {
        float parallaxStrength = 40;
        float targetParallaxX = (float) (client.mouse.getX() - (double) client.getWindow().getWidth() / 2) / client.getWindow().getWidth() * parallaxStrength;
        float targetParallaxY = (float) (client.mouse.getY() - (double) client.getWindow().getHeight() / 2) / client.getWindow().getHeight() * parallaxStrength;

        parallaxX += (targetParallaxX - parallaxX) * 0.1f;
        parallaxY += (targetParallaxY - parallaxY) * 0.1f;

        float backgroundScale = 1.2f;
        float scaledWidth = client.getWindow().getWidth() * backgroundScale;
        float scaledHeight = client.getWindow().getHeight() * backgroundScale;

        float offsetX = (scaledWidth - client.getWindow().getWidth()) / 2 - parallaxX;
        float offsetY = (scaledHeight - client.getWindow().getHeight()) / 2 - parallaxY;

        Skia.drawImage("background.png", -offsetX, -offsetY, scaledWidth, scaledHeight);
    }
}
