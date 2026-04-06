package cn.pupperclient.gui.welcomegui;

import cn.pupperclient.PupperClient;
import cn.pupperclient.gui.api.SimpleSoarGui;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.ui.component.handler.impl.ButtonHandler;
import cn.pupperclient.ui.component.impl.Button;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.sounds.SoundEvents;

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
        acceptButton = new Button("text.accept", 0, 0, Button.Style.TONAL);
        acceptButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                accepted = true;
                if (client.player != null) {
                    client.player.playSound(SoundEvents.UI_TOAST_IN, 1.0f, 1.0f);
                }
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

        rebuildLayout();
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        super.resize(client, width, height);
        rebuildLayout();
    }

    private void updatePositions() {
        centerX = client.getWindow().getWidth() / 2;
        centerY = client.getWindow().getHeight() / 2;
    }

    private void rebuildLayout() {
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
        drawTranslucentBackground();

        renderSkijaWelcome(mouseX, mouseY);
    }

    private void drawTranslucentBackground() {
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
    public boolean onMousePressed(double mouseX, double mouseY, int button) {
        acceptButton.mousePressed(mouseX, mouseY, button);
        declineButton.mousePressed(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        acceptButton.mouseReleased(mouseX, mouseY, button);
        declineButton.mouseReleased(mouseX, mouseY, button);
        return true;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
