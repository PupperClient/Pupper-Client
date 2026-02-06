package cn.pupperclient.gui.welcomegui;

import cn.pupperclient.PupperClient;
import cn.pupperclient.gui.api.SimplePupperGui;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.ui.component.handler.impl.ButtonHandler;
import cn.pupperclient.ui.component.impl.Button;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.sound.SoundEvents;

import java.awt.Color;

public class TermsScreen extends SimplePupperGui {
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
                assert client.player != null;
                client.player.playSound(SoundEvents.UI_TOAST_IN, 1.0f, 1.0f);
                PupperClient.hasAcceptedTerms = true;
                client.setScreen(null);
            }
        });

        declineButton = new Button("text.decline", 0, 0, Button.Style.TONAL);
        declineButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                client.setScreen(new TitleScreen());
            }
        });

        updateButtonPositions();
    }

    private void updatePositions() {
        centerX = client.getWindow().getWidth() / 2;
        centerY = client.getWindow().getHeight() / 2;
    }

    private void updateButtonPositions() {
        updatePositions();

        float acceptWidth = acceptButton.getWidth();
        float declineWidth = declineButton.getWidth();

        float totalWidth = acceptWidth + declineWidth + 10;
        float startX = centerX - totalWidth / 2;

        acceptButton.setX(startX);
        acceptButton.setY(centerY + 20);

        declineButton.setX(startX + acceptWidth + 10);
        declineButton.setY(centerY + 20);
    }

    @Override
    public void draw(double mouseX, double mouseY) {
        updatePositions();
        updateButtonPositions();

        drawTranslucentBackground();

        renderSkijaWelcome(mouseX, mouseY);
    }

    private void drawTranslucentBackground() {
        // 绘制半透明黑色背景
        Color translucentBlack = new Color(0, 0, 0, 180);
        Skia.drawRect(0, 0, client.getWindow().getWidth(), client.getWindow().getHeight(), translucentBlack);
    }

    private void renderSkijaWelcome(double mouseX, double mouseY) {
        Skia.drawFullCenteredText("Terms of Service", centerX, centerY - 60, Color.WHITE, Fonts.getRegular(20));

        Skia.drawFullCenteredText("Please read and accept the Terms of Service",
            centerX, centerY - 20, Color.WHITE, Fonts.getRegular(14));

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

    public boolean isAccepted() {
        return accepted;
    }
}
