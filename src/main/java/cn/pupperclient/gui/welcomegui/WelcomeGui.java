package cn.pupperclient.gui.welcomegui;

import cn.pupperclient.Soar;
import cn.pupperclient.animation.SimpleAnimation;
import cn.pupperclient.gui.MainMenuGui;
import cn.pupperclient.gui.api.SimpleSoarGui;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.utils.ColorUtils;
import net.minecraft.client.gui.screen.Screen;

import java.awt.*;

public class WelcomeGui extends SimpleSoarGui {
    private final SimpleAnimation fadeInAnimation = new SimpleAnimation();
    private final SimpleAnimation textAnimation = new SimpleAnimation();
    private final SimpleAnimation scaleAnimation = new SimpleAnimation();
    private final SimpleAnimation fadeOutAnimation = new SimpleAnimation();
    private boolean transitionStarted = false;
    private Screen nextScreen;
    private long startTime;

    public WelcomeGui() {
        super(false);
    }

    @Override
    public void init() {
        // 初始化所有动画
        fadeInAnimation.setValue(0);
        textAnimation.setValue(0);
        scaleAnimation.setValue(0);
        fadeOutAnimation.setValue(0);

        transitionStarted = false;
        startTime = System.currentTimeMillis();

        // 开始淡入动画
        fadeInAnimation.onTick(1, 30); // 1.5秒淡入
    }

    @Override
    public void draw(double mouseX, double mouseY) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;

        // 获取颜色调色板
        ColorPalette palette = Soar.getInstance().getColorManager().getPalette();

        // 绘制半透明黑色背景
        Skia.drawRect(0, 0, client.getWindow().getWidth(), client.getWindow().getHeight(),
            ColorUtils.applyAlpha(Color.BLACK, 0.8f * fadeInAnimation.getValue()));

        // 应用整体淡入淡出效果
        Skia.setAlpha((int) (255 * fadeInAnimation.getValue() * (1 - fadeOutAnimation.getValue())));

        // 计算中心位置
        float centerX = client.getWindow().getWidth() / 2f;
        float centerY = client.getWindow().getHeight() / 2f;

        // 应用缩放动画
        float scale = 0.8f + 0.2f * scaleAnimation.getValue();
        Skia.save();
        Skia.scale(centerX, centerY, scale);

        // 绘制欢迎卡片背景
        float cardWidth = 600;
        float cardHeight = 300;
        float cardX = centerX - cardWidth / 2;
        float cardY = centerY - cardHeight / 2;
        float borderRadius = 25;

        // 卡片背景（带模糊效果）
        Skia.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, borderRadius,
            ColorUtils.applyAlpha(palette.getSurfaceContainer(), 0.9f));

        // 绘制发光边框效果
        if (scaleAnimation.getValue() > 0) {
            Skia.drawOutline(cardX - 2, cardY - 2, cardWidth + 4, cardHeight + 4,
                borderRadius + 2, 3,
                ColorUtils.applyAlpha(palette.getPrimary(), scaleAnimation.getValue() * 0.5f));
        }

        // 绘制Logo
        float logoSize = 80;
        float logoX = centerX - logoSize / 2;
        float logoY = cardY + 40;
        Skia.drawRoundedImage("logo.png", logoX, logoY, logoSize, logoSize, 15);

        // 绘制欢迎文字（打字机效果）
        String welcomeText = "Welcome to Soar CN Client";
        float textY = logoY + logoSize + 30;

        // 计算文字显示进度
        int textLength = welcomeText.length();
        int visibleChars = (int) (textLength * textAnimation.getValue());
        String visibleText = welcomeText.substring(0, Math.min(visibleChars, textLength));

        // 绘制主标题
        Skia.drawCenteredText(visibleText, centerX, textY,
            ColorUtils.applyAlpha(palette.getOnSurface(), textAnimation.getValue()),
            Fonts.getRegular(28));

        // 绘制光标（打字机效果）
        if (textAnimation.getValue() < 1 && System.currentTimeMillis() % 1000 < 500) {
            float textWidth = Skia.getTextWidth(visibleText, Fonts.getRegular(28));
            Skia.drawRect(centerX + textWidth / 2, textY + 2, 2, 25, palette.getPrimary());
        }

        // 绘制副标题（在主要文字显示完成后出现）
        if (textAnimation.getValue() >= 1) {
            String subtitle = "沉浸式 Minecraft 客户端体验";
            float subtitleY = textY + 40;
            float subtitleAlpha = Math.max(0, (textAnimation.getValue() - 1) * 2); // 延迟显示

            Skia.drawCenteredText(subtitle, centerX, subtitleY,
                ColorUtils.applyAlpha(palette.getOnSurfaceVariant(), subtitleAlpha),
                Fonts.getRegular(16));
        }

        // 绘制加载进度条
        if (textAnimation.getValue() >= 1) {
            float progressBarWidth = 400;
            float progressBarHeight = 4;
            float progressBarX = centerX - progressBarWidth / 2;
            float progressBarY = cardY + cardHeight - 40;

            // 背景
            Skia.drawRoundedRect(progressBarX, progressBarY, progressBarWidth, progressBarHeight, 2,
                ColorUtils.applyAlpha(palette.getSurfaceVariant(), 0.5f));

            // 进度
            float progress = fadeOutAnimation.getValue();
            if (progress > 0) {
                Skia.drawRoundedRect(progressBarX, progressBarY, progressBarWidth * progress, progressBarHeight, 2,
                    palette.getPrimary());
            }
        }

        Skia.restore(); // 恢复缩放

        // 动画时间轴控制
        controlAnimationTimeline(elapsed);
    }

    private void controlAnimationTimeline(long elapsed) {
        // 时间轴序列：
        // 0-1500ms: 淡入
        // 1500-2500ms: 文字打字效果
        // 2500-3500ms: 缩放脉冲效果
        // 3500-4500ms: 淡出过渡

        if (elapsed > 1500 && textAnimation.getValue() < 1) {
            textAnimation.onTick(1, 40); // 2.5秒完成打字效果
        }

        if (elapsed > 2500 && scaleAnimation.getValue() < 1) {
            scaleAnimation.onTick(1, 20); // 1秒缩放效果
        }

        if (elapsed > 3500 && !transitionStarted) {
            transitionStarted = true;
            fadeOutAnimation.onTick(1, 25); // 1.25秒淡出

            // 延迟切换到主菜单
            new Thread(() -> {
                try {
                    Thread.sleep(1250); // 等待淡出动画完成
                    if (client != null) {
                        client.execute(() -> {
                            if (nextScreen != null) {
                                client.setScreen(nextScreen);
                            } else {
                                client.setScreen(new MainMenuGui().build());
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    public void setNextScreen(Screen nextScreen) {
        this.nextScreen = nextScreen;
    }

    @Override
    public void mousePressed(double mouseX, double mouseY, int button) {
        // 点击跳过欢迎界面
        if (!transitionStarted) {
            transitionStarted = true;
            fadeOutAnimation.setValue(1);
            client.setScreen(new MainMenuGui().build());
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        // 按键跳过欢迎界面
        if (!transitionStarted) {
            transitionStarted = true;
            fadeOutAnimation.setValue(1);
            client.setScreen(new MainMenuGui().build());
        }
    }
}
