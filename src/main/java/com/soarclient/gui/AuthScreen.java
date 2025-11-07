package com.soarclient.gui;

import com.soarclient.management.auth.AuthManager;
import com.soarclient.skia.Skia;
import com.soarclient.skia.font.Fonts;
import com.soarclient.utils.IMinecraft;
import com.soarclient.utils.mouse.MouseUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;

public class AuthScreen extends Screen implements IMinecraft {
    private enum AuthMode {
        LOGIN, REGISTER
    }

    private AuthMode currentMode = AuthMode.LOGIN;
    private String username = "";
    private String password = "";
    private String message = "";
    private Color messageColor = Color.WHITE;

    // UI elements
    private float usernameFieldX;
    private float usernameFieldY;
    private final float usernameFieldWidth = 200;
    private final float usernameFieldHeight = 30;
    private float passwordFieldX;
    private float passwordFieldY;
    private final float passwordFieldWidth = 200;
    private final float passwordFieldHeight = 30;
    private float loginButtonX;
    private float loginButtonY;
    private final float loginButtonWidth = 90;
    private final float loginButtonHeight = 30;
    private float registerButtonX;
    private float registerButtonY;
    private final float registerButtonWidth = 90;
    private final float registerButtonHeight = 30;
    private float switchButtonX;
    private float switchButtonY;
    private final float switchButtonWidth = 180;
    private final float switchButtonHeight = 25;

    private boolean usernameSelected = false;
    private boolean passwordSelected = false;

    public AuthScreen() {
        super(Text.of("SoarClient Authentication"));
        calculatePositions();
    }

    private void calculatePositions() {
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        float centerX = screenWidth / 2f;
        float centerY = screenHeight / 2f;

        usernameFieldX = centerX - usernameFieldWidth / 2;
        usernameFieldY = centerY - 60;
        passwordFieldX = centerX - passwordFieldWidth / 2;
        passwordFieldY = centerY - 20;

        loginButtonX = centerX - loginButtonWidth - 5;
        loginButtonY = centerY + 20;
        registerButtonX = centerX + 5;
        registerButtonY = centerY + 20;

        switchButtonX = centerX - switchButtonWidth / 2;
        switchButtonY = centerY + 70;
    }

    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        // 绘制半透明背景
        Skia.drawRect(0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(),
            new Color(0, 0, 0, 200));

        // 绘制标题
        String title = currentMode == AuthMode.LOGIN ? "SoarClient - Login" : "SoarClient - Register";
        Skia.drawText(title, mc.getWindow().getScaledWidth() / 2f - 80, 50,
            new Color(66, 135, 245), Fonts.getMedium(24));

        // 绘制输入框标签
        Skia.drawText("Username:", usernameFieldX, usernameFieldY - 15, Color.WHITE, Fonts.getRegular(14));
        Skia.drawText("Password:", passwordFieldX, passwordFieldY - 15, Color.WHITE, Fonts.getRegular(14));

        // 绘制输入框背景
        Color fieldBg = new Color(40, 40, 40, 255);
        Color fieldBorder = usernameSelected ? new Color(66, 135, 245) : new Color(80, 80, 80);
        Skia.drawRect(usernameFieldX, usernameFieldY, usernameFieldWidth, usernameFieldHeight, fieldBg);
        Skia.drawRect(usernameFieldX, usernameFieldY, usernameFieldWidth, usernameFieldHeight, fieldBorder);

        fieldBorder = passwordSelected ? new Color(66, 135, 245) : new Color(80, 80, 80);
        Skia.drawRect(passwordFieldX, passwordFieldY, passwordFieldWidth, passwordFieldHeight, fieldBg);
        Skia.drawRect(passwordFieldX, passwordFieldY, passwordFieldWidth, passwordFieldHeight, fieldBorder);

        // 绘制输入文本
        Skia.drawText(username, usernameFieldX + 5, usernameFieldY + 8, Color.WHITE, Fonts.getRegular(16));
        Skia.drawText(getMaskedPassword(), passwordFieldX + 5, passwordFieldY + 8, Color.WHITE, Fonts.getRegular(16));

        // 绘制按钮
        drawButton(loginButtonX, loginButtonY, loginButtonWidth, loginButtonHeight,
            "Login", mouseX, mouseY, currentMode == AuthMode.LOGIN);
        drawButton(registerButtonX, registerButtonY, registerButtonWidth, registerButtonHeight,
            "Register", mouseX, mouseY, currentMode == AuthMode.REGISTER);

        // 绘制切换模式按钮
        String switchText = currentMode == AuthMode.LOGIN ?
            "Don't have an account? Register" : "Already have an account? Login";
        drawButton(switchButtonX, switchButtonY, switchButtonWidth, switchButtonHeight,
            switchText, mouseX, mouseY, false);

        // 绘制消息
        if (!message.isEmpty()) {
            Skia.drawText(message, mc.getWindow().getScaledWidth() / 2f - 100,
                switchButtonY + 40, messageColor, Fonts.getRegular(14));
        }

        // 绘制光标
        if (System.currentTimeMillis() % 1000 < 500) {
            float cursorX = usernameSelected ?
                usernameFieldX + 5 + Skia.getTextWidth(username, Fonts.getRegular(16)) :
                passwordFieldX + 5 + Skia.getTextWidth(getMaskedPassword(), Fonts.getRegular(16));
            float cursorY = usernameSelected ? usernameFieldY + 5 : passwordFieldY + 5;
            float cursorHeight = usernameSelected ? usernameFieldHeight - 10 : passwordFieldHeight - 10;

            if (usernameSelected || passwordSelected) {
                Skia.drawRect(cursorX, cursorY, 2, cursorHeight, Color.WHITE);
            }
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void drawButton(float x, float y, float width, float height, String text,
                            int mouseX, int mouseY, boolean isActive) {
        boolean isHovered = MouseUtils.isHovered(x, y, width, height, mouseX, mouseY);

        Color bgColor;
        if (isActive) {
            bgColor = new Color(66, 135, 245);
        } else {
            bgColor = isHovered ? new Color(80, 80, 80) : new Color(60, 60, 60);
        }

        Skia.drawRect(x, y, width, height, bgColor);
        Skia.drawRect(x, y, width, height, new Color(100, 100, 100));

        float textWidth = Skia.getTextWidth(text, Fonts.getRegular(14));
        float textX = x + (width - textWidth) / 2;
        float textY = y + (height - 10) / 2;

        Skia.drawText(text, textX, textY, Color.WHITE, Fonts.getRegular(14));
    }

    private String getMaskedPassword() {
        return "*".repeat(password.length());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查输入框点击
        usernameSelected = MouseUtils.isHovered(usernameFieldX, usernameFieldY,
            usernameFieldWidth, usernameFieldHeight,
            (int) mouseX, (int) mouseY);
        passwordSelected = MouseUtils.isHovered(passwordFieldX, passwordFieldY,
            passwordFieldWidth, passwordFieldHeight,
            (int) mouseX, (int) mouseY);

        // 检查登录按钮点击
        if (MouseUtils.isHovered(loginButtonX, loginButtonY, loginButtonWidth, loginButtonHeight,
            (int) mouseX, (int) mouseY)) {
            attemptLogin();
            return true;
        }

        // 检查注册按钮点击
        if (MouseUtils.isHovered(registerButtonX, registerButtonY, registerButtonWidth, registerButtonHeight,
            (int) mouseX, (int) mouseY)) {
            attemptRegister();
            return true;
        }

        // 检查切换模式按钮点击
        if (MouseUtils.isHovered(switchButtonX, switchButtonY, switchButtonWidth, switchButtonHeight,
            (int) mouseX, (int) mouseY)) {
            currentMode = currentMode == AuthMode.LOGIN ? AuthMode.REGISTER : AuthMode.LOGIN;
            message = "";
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 处理退格键
        if (keyCode == 259) { // Backspace
            if (usernameSelected && !username.isEmpty()) {
                username = username.substring(0, username.length() - 1);
            } else if (passwordSelected && !password.isEmpty()) {
                password = password.substring(0, password.length() - 1);
            }
            return true;
        }

        // 处理回车键
        if (keyCode == 257) { // Enter
            if (currentMode == AuthMode.LOGIN) {
                attemptLogin();
            } else {
                attemptRegister();
            }
            return true;
        }

        // 处理Tab键切换输入框
        if (keyCode == 258) { // Tab
            if (usernameSelected) {
                usernameSelected = false;
                passwordSelected = true;
            } else if (passwordSelected) {
                passwordSelected = false;
                usernameSelected = true;
            } else {
                usernameSelected = true;
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (usernameSelected) {
            username += chr;
            return true;
        } else if (passwordSelected) {
            password += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    private void attemptLogin() {
        if (username.isEmpty() || password.isEmpty()) {
            setMessage("Please fill in all fields", new Color(255, 100, 100));
            return;
        }

        AuthManager authManager = AuthManager.getInstance();
        if (authManager.login(username, password)) {
            setMessage("Login successful!", new Color(100, 255, 100));
            // 延迟关闭屏幕，让用户看到成功消息
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    mc.execute(() -> {
                        if (mc.currentScreen == this) {
                            mc.setScreen(null);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            setMessage("Invalid username or password", new Color(255, 100, 100));
        }
    }

    private void attemptRegister() {
        if (username.isEmpty() || password.isEmpty()) {
            setMessage("Please fill in all fields", new Color(255, 100, 100));
            return;
        }

        if (username.length() < 3) {
            setMessage("Username must be at least 3 characters", new Color(255, 100, 100));
            return;
        }

        if (password.length() < 4) {
            setMessage("Password must be at least 4 characters", new Color(255, 100, 100));
            return;
        }

        AuthManager authManager = AuthManager.getInstance();
        if (authManager.register(username, password)) {
            setMessage("Registration successful! You can now login", new Color(100, 255, 100));
            currentMode = AuthMode.LOGIN;
        } else {
            setMessage("Username already exists", new Color(255, 100, 100));
        }
    }

    private void setMessage(String message, Color color) {
        this.message = message;
        this.messageColor = color;
    }

    @Override
    public boolean shouldPause() {
        return false; // 不需要暂停游戏
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // 禁用ESC关闭
    }
}
