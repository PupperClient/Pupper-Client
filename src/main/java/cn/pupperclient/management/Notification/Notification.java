package cn.pupperclient.management.Notification;

import cn.pupperclient.Soar;
import cn.pupperclient.animation.SimpleAnimation;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.utils.ColorUtils;

public class Notification {
    String message;
    String icon;
    Soar.MusicToolStatus status;
    float progress;
    SimpleAnimation animation = new SimpleAnimation();
    long createTime;
    long updateTime;
    boolean removing = false;

    public Notification(String message, String icon, Soar.MusicToolStatus status, float progress) {
        this.message = message;
        this.icon = icon;
        this.status = status;
        this.progress = progress;
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
        this.animation.setValue(0);
    }

    private java.awt.Color getIconColor() {
        ColorPalette palette = Soar.getInstance().getColorManager().getPalette();
        return switch (status) {
            case INSTALLED, DONE -> new java.awt.Color(76, 175, 80);
            case FAILED -> palette.getError();
            case CHECKING, DOWNLOADING -> palette.getWarning();
        };
    }

    private java.awt.Color getProgressColor() {
        ColorPalette palette = Soar.getInstance().getColorManager().getPalette();
        return switch (status) {
            case INSTALLED, DONE -> new java.awt.Color(76, 175, 80); // 绿色
            case FAILED -> palette.getError();
            default -> palette.getPrimary();
        };
    }

    public void draw(float x, float y, float width, float height) {
        // 更新动画
        if (!removing) {
            animation.onTick(1, 20);

            // 只有安装完成或失败的状态才自动移除
            long displayTime = System.currentTimeMillis() - createTime;
            if ((status == Soar.MusicToolStatus.INSTALLED || status == Soar.MusicToolStatus.DONE) && displayTime > 3000) {
                removing = true;
            } else if (status == Soar.MusicToolStatus.FAILED && displayTime > 5000) {
                removing = true;
            }
        } else {
            animation.onTick(0, 20);
        }

        if (animation.getValue() <= 0.01f) {
            return;
        }

        ColorPalette palette = Soar.getInstance().getColorManager().getPalette();

        // 背景
        java.awt.Color bgColor = ColorUtils.applyAlpha(palette.getSurfaceContainer(),
            animation.getValue() * 0.95f);
        Skia.drawRoundedRect(x, y, width, height, 12, bgColor);

        // 图标 - 使用正确的字体渲染
        float iconSize = 28;
        float iconX = x + 20;
        float iconY = y + (height - iconSize) / 2;

        java.awt.Color iconColor = getIconColor();
        // 使用 Skia 的图标绘制方法
        Skia.drawFullCenteredText(icon, iconX + iconSize/2, iconY + iconSize/2, iconColor, Fonts.getIcon(iconSize));

        // 文本
        float textX = iconX + iconSize + 15;
        float textY = y + 25;
        java.awt.Color textColor = ColorUtils.applyAlpha(palette.getOnSurface(), animation.getValue());
        Skia.drawText(message, textX, textY, textColor, Fonts.getRegular(14));

        // 进度文本
        if (status == Soar.MusicToolStatus.DOWNLOADING) {
            String progressText = String.format("%.0f%%", progress * 100);
            Skia.drawText(progressText, textX, textY + 18, textColor, Fonts.getRegular(12));
        }

        // 进度条
        drawProgressBar(x, y, width, height, progress);
    }

    private void drawProgressBar(float x, float y, float width, float height, float progress) {
        ColorPalette palette = Soar.getInstance().getColorManager().getPalette();

        float barHeight = 4;
        float barY = y + height - 15;
        float barWidth = width - 40;
        float barX = x + 20;

        // 背景条
        Skia.drawRoundedRect(barX, barY, barWidth, barHeight, 2,
            ColorUtils.applyAlpha(palette.getSurface(), 0.3f));

        // 进度条
        if (progress > 0) {
            float progressWidth = barWidth * progress;
            java.awt.Color progressColor = getProgressColor();
            Skia.drawRoundedRect(barX, barY, progressWidth, barHeight, 2, progressColor);
        }

        // 加载动画（用于 CHECKING 状态）
        if (status == Soar.MusicToolStatus.CHECKING) {
            drawLoadingAnimation(barX, barY, barWidth, barHeight);
        }
    }

    private void drawLoadingAnimation(float x, float y, float width, float height) {
        ColorPalette palette = Soar.getInstance().getColorManager().getPalette();

        long time = System.currentTimeMillis();
        float phase = (time % 1000) / 1000.0f;
        float pulse = (float) (0.5 + 0.5 * Math.sin(phase * Math.PI * 2));

        java.awt.Color pulseColor = ColorUtils.applyAlpha(palette.getPrimary(), pulse * 0.8f);
        Skia.drawRoundedRect(x, y, width, height, 2, pulseColor);
    }

    public boolean shouldRemove() {
        return removing && animation.getValue() <= 0.01f;
    }
}
