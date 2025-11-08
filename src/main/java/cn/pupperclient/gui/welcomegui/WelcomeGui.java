package cn.pupperclient.gui.welcomegui;

import cn.pupperclient.PupperClient;
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
    private final SimpleAnimation textFadeAnimation = new SimpleAnimation();
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
        textFadeAnimation.setValue(0);
        fadeOutAnimation.setValue(0);

        transitionStarted = false;
        startTime = System.currentTimeMillis();

        // 开始淡入动画
        fadeInAnimation.onTick(1, 30); // 1.5秒淡入

        // 文字淡入动画稍晚开始
        new Thread(() -> {
            try {
                Thread.sleep(800); // 延迟0.8秒后开始文字淡入
                textFadeAnimation.onTick(1, 40); // 2秒淡入

                // 文字保持显示一段时间后开始淡出
                Thread.sleep(2500); // 保持2.5秒
                textFadeAnimation.onTick(0, 40); // 2秒淡出

                // 文字淡出完成后开始整体淡出
                Thread.sleep(2000); // 等待淡出完成
                if (!transitionStarted) {
                    transitionStarted = true;
                    fadeOutAnimation.onTick(1, 25); // 1.25秒整体淡出

                    // 切换到主菜单
                    Thread.sleep(1250);
                    if (client != null) {
                        client.execute(() -> {
                            if (nextScreen != null) {
                                client.setScreen(nextScreen);
                            } else {
                                client.setScreen(new MainMenuGui().build());
                            }
                        });
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void draw(double mouseX, double mouseY) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;

        // 获取颜色调色板
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

        // 绘制半透明黑色背景
        Skia.drawRect(0, 0, client.getWindow().getWidth(), client.getWindow().getHeight(),
            ColorUtils.applyAlpha(Color.BLACK, 0.9f * fadeInAnimation.getValue()));

        // 应用整体淡入淡出效果
        Skia.setAlpha((int) (255 * fadeInAnimation.getValue() * (1 - fadeOutAnimation.getValue())));

        // 计算中心位置
        float centerX = client.getWindow().getWidth() / 2f;
        float centerY = client.getWindow().getHeight() / 2f;

        // 绘制欢迎卡片背景
        float cardWidth = 500;
        float cardHeight = 300;
        float cardX = centerX - cardWidth / 2;
        float cardY = centerY - cardHeight / 2;
        float borderRadius = 20;

        // 卡片背景
        Skia.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, borderRadius,
            ColorUtils.applyAlpha(palette.getSurfaceContainer(), 0.95f));

        // 绘制Logo
        float logoSize = 80;
        float logoX = centerX - logoSize / 2;
        float logoY = cardY + 40;
        Skia.drawRoundedImage("logo.png", logoX, logoY, logoSize, logoSize, 15);

        String welcomeText = "Welcome to Pupper Client";
        float textY = logoY + logoSize + 50;

        // 使用textFadeAnimation控制文字透明度
        Skia.drawCenteredText(welcomeText, centerX, textY,
            ColorUtils.applyAlpha(palette.getOnSurface(), textFadeAnimation.getValue()),
            Fonts.getRegular(32));

        // 如果用户点击或按键，立即跳过
        if (transitionStarted) {
            Skia.setAlpha((int) (255 * (1 - fadeOutAnimation.getValue())));
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
