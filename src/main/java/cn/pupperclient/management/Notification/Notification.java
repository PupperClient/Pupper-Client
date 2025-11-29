package cn.pupperclient.management.Notification;

import cn.pupperclient.PupperClient;
import cn.pupperclient.animation.SimpleAnimation;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.utils.ColorUtils;
import cn.pupperclient.utils.ExternalToolManager;

public class Notification {
    String message;
    String icon;
    PupperClient.MusicToolStatus status;
    float progress;
    SimpleAnimation animation = new SimpleAnimation();
    long createTime;
    long updateTime;
    boolean removing = false;

    // 多进度支持
    private boolean showMultiProgress = false;
    private float ytDlpProgress = 0f;
    private float ffmpegProgress = 0f;
    private String currentDownload = "";

    public Notification(String message, String icon, PupperClient.MusicToolStatus status, float progress) {
        this.message = message;
        this.icon = icon;
        this.status = status;
        this.progress = progress;
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
        this.animation.setValue(0);
    }

    /**
     * 设置多进度显示模式
     */
    public void setMultiProgressMode(boolean enabled) {
        this.showMultiProgress = enabled;
    }

    /**
     * 更新多进度信息
     */
    public void updateMultiProgress(float ytDlpProgress, float ffmpegProgress, String currentDownload) {
        this.ytDlpProgress = ytDlpProgress;
        this.ffmpegProgress = ffmpegProgress;
        this.currentDownload = currentDownload;
        this.updateTime = System.currentTimeMillis();
    }

    private java.awt.Color getIconColor() {
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();
        return switch (status) {
            case INSTALLED, DONE -> new java.awt.Color(76, 175, 80);
            case FAILED -> palette.getError();
            case CHECKING, DOWNLOADING -> palette.getWarning();
        };
    }

    private java.awt.Color getProgressColor() {
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();
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
            if ((status == PupperClient.MusicToolStatus.INSTALLED || status == PupperClient.MusicToolStatus.DONE) && displayTime > 3000) {
                removing = true;
            } else if (status == PupperClient.MusicToolStatus.FAILED && displayTime > 5000) {
                removing = true;
            }
        } else {
            animation.onTick(0, 20);
        }

        if (animation.getValue() <= 0.01f) {
            return;
        }

        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

        // 背景
        java.awt.Color bgColor = ColorUtils.applyAlpha(palette.getSurfaceContainer(),
            animation.getValue() * 0.95f);
        Skia.drawRoundedRect(x, y, width, height, 12, bgColor);

        // 图标
        float iconSize = 28;
        float iconX = x + 20;
        float iconY = y + (height - iconSize) / 2;

        java.awt.Color iconColor = getIconColor();
        Skia.drawFullCenteredText(icon, iconX + iconSize/2, iconY + iconSize/2, iconColor, Fonts.getIcon(iconSize));

        // 文本
        float textX = iconX + iconSize + 15;
        float textY = y + 25;
        java.awt.Color textColor = ColorUtils.applyAlpha(palette.getOnSurface(), animation.getValue());
        Skia.drawText(message, textX, textY, textColor, Fonts.getRegular(14));

        if (showMultiProgress) {
            // 多进度显示模式
            drawMultiProgressBars(x, y, width, height);
        } else {
            // 单进度显示模式
            drawSingleProgress(x, y, width, height);
        }
    }

    /**
     * 绘制单进度条
     */
    private void drawSingleProgress(float x, float y, float width, float height) {
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

        float textX = x + 75;
        float textY = y + 25;

        // 进度文本
        if (status == PupperClient.MusicToolStatus.DOWNLOADING) {
            String progressText = String.format("%.0f%%", progress * 100);
            Skia.drawText(progressText, textX, textY + 18,
                ColorUtils.applyAlpha(palette.getOnSurface(), animation.getValue()),
                Fonts.getRegular(12));
        }

        // 进度条
        drawProgressBar(x, y, width, height, progress, false);
    }

    /**
     * 绘制多进度条
     */
    private void drawMultiProgressBars(float x, float y, float width, float height) {
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();
        java.awt.Color textColor = ColorUtils.applyAlpha(palette.getOnSurface(), animation.getValue());

        float textX = x + 75;
        float textY = y + 25;

        // YT-DLP 进度
        String ytDlpText = String.format("YT-DLP: %.0f%%", ytDlpProgress * 100);
        Skia.drawText(ytDlpText, textX, textY + 18, textColor, Fonts.getRegular(11));

        // FFmpeg 进度
        String ffmpegText = String.format("FFmpeg: %.0f%%", ffmpegProgress * 100);
        Skia.drawText(ffmpegText, textX, textY + 32, textColor, Fonts.getRegular(11));

        // 当前下载项
        if (!currentDownload.isEmpty()) {
            String currentText = "当前: " + currentDownload;
            Skia.drawText(currentText, textX, textY + 46, textColor, Fonts.getRegular(10));
        }

        // 双进度条
        drawDoubleProgressBar(x, y, width, height);
    }

    /**
     * 绘制双进度条
     */
    private void drawDoubleProgressBar(float x, float y, float width, float height) {
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

        float barHeight = 3;
        float barSpacing = 2;
        float barY = y + height - 20;
        float barWidth = width - 40;
        float barX = x + 20;

        // YT-DLP 进度条背景
        Skia.drawRoundedRect(barX, barY, barWidth, barHeight, 1,
            ColorUtils.applyAlpha(palette.getSurface(), 0.3f));

        // YT-DLP 进度条
        if (ytDlpProgress > 0) {
            float progressWidth = barWidth * ytDlpProgress;
            java.awt.Color progressColor = getProgressColor();
            Skia.drawRoundedRect(barX, barY, progressWidth, barHeight, 1, progressColor);
        }

        // FFmpeg 进度条背景
        float ffmpegBarY = barY + barHeight + barSpacing;
        Skia.drawRoundedRect(barX, ffmpegBarY, barWidth, barHeight, 1,
            ColorUtils.applyAlpha(palette.getSurface(), 0.3f));

        // FFmpeg 进度条
        if (ffmpegProgress > 0) {
            float progressWidth = barWidth * ffmpegProgress;
            java.awt.Color progressColor = new java.awt.Color(33, 150, 243); // 蓝色
            Skia.drawRoundedRect(barX, ffmpegBarY, progressWidth, barHeight, 1, progressColor);
        }

        // 加载动画（用于 CHECKING 状态）
        if (status == PupperClient.MusicToolStatus.CHECKING) {
            drawLoadingAnimation(barX, barY, barWidth, barHeight * 2 + barSpacing);
        }
    }

    private void drawProgressBar(float x, float y, float width, float height, float progress, boolean isMulti) {
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

        float barHeight = isMulti ? 3 : 4;
        float barY = y + height - (isMulti ? 20 : 15);
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
        if (status == PupperClient.MusicToolStatus.CHECKING) {
            drawLoadingAnimation(barX, barY, barWidth, barHeight);
        }
    }

    private void drawLoadingAnimation(float x, float y, float width, float height) {
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

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
