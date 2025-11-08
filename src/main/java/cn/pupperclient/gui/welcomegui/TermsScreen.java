package cn.pupperclient.gui.welcomegui;

import cn.pupperclient.PupperClient;
import cn.pupperclient.gui.api.SimpleSoarGui;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.ui.component.handler.impl.ButtonHandler;
import cn.pupperclient.ui.component.impl.Button;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.sound.SoundEvents;

import java.awt.Color;

public class TermsScreen extends SimpleSoarGui {
    private int centerX;
    private int centerY;
    private boolean accepted = false;

    private Button acceptButton;
    private Button declineButton;

    public TermsScreen() {
        super(false);
    }

    @Override
    public void init() {
        updatePositions();

        acceptButton = new Button("text.accept", 0, 0, Button.Style.TONAL);
        acceptButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                accepted = true;
                client.player.playSound(SoundEvents.UI_TOAST_IN, 1.0f, 1.0f);
                PupperClient.hasAcceptedTerms = true;
                client.setScreen(null);
            }
        });

        // 创建拒绝按钮
        declineButton = new Button("text.decline", 0, 0, Button.Style.TONAL);
        declineButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                client.setScreen(new TitleScreen());
            }
        });

        // 更新按钮位置
        updateButtonPositions();
    }

    private void updatePositions() {
        centerX = client.getWindow().getWidth() / 2;
        centerY = client.getWindow().getHeight() / 2;
    }

    private void updateButtonPositions() {
        updatePositions();

        // 获取按钮宽度
        float acceptWidth = acceptButton.getWidth();
        float declineWidth = declineButton.getWidth();

        // 计算按钮位置 - 让两个按钮紧挨着居中显示
        float totalWidth = acceptWidth + declineWidth + 10; // 10像素间距
        float startX = centerX - totalWidth / 2;

        acceptButton.setX(startX);
        acceptButton.setY(centerY + 20);

        declineButton.setX(startX + acceptWidth + 10); // 10像素间距
        declineButton.setY(centerY + 20);
    }

    @Override
    public void draw(double mouseX, double mouseY) {
        // 更新位置
        updatePositions();
        updateButtonPositions();

        // 绘制半透明背景
        drawTranslucentBackground();

        // 绘制欢迎界面内容
        renderSkijaWelcome(mouseX, mouseY);
    }

    private void drawTranslucentBackground() {
        // 绘制半透明黑色背景
        Color translucentBlack = new Color(0, 0, 0, 180);
        Skia.drawRect(0, 0, client.getWindow().getWidth(), client.getWindow().getHeight(), translucentBlack);
    }

    private void renderSkijaWelcome(double mouseX, double mouseY) {
        // 绘制标题
        Skia.drawFullCenteredText("Terms of Service", centerX, centerY - 60, Color.WHITE, Fonts.getRegular(20));

        // 绘制正文 - 增加与标题的间距
        Skia.drawFullCenteredText("Please read and accept the Terms of Service",
            centerX, centerY - 20, Color.WHITE, Fonts.getRegular(14));

        // 绘制按钮
        acceptButton.draw(mouseX, mouseY);
        declineButton.draw(mouseX, mouseY);
    }

    @Override
    public void mousePressed(double mouseX, double mouseY, int button) {
        acceptButton.mousePressed(mouseX, mouseY, button);
        declineButton.mousePressed(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        acceptButton.mouseReleased(mouseX, mouseY, button);
        declineButton.mouseReleased(mouseX, mouseY, button);
    }

    // 添加一个方法来检查是否已经接受条款
    public boolean isAccepted() {
        return accepted;
    }
}
