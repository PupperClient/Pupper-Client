package cn.pupperclient.gui;

import cn.pupperclient.PupperClient;
import cn.pupperclient.animation.SimpleAnimation;
import cn.pupperclient.gui.api.SimpleSoarGui;
import cn.pupperclient.management.auth.AuthManager;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.utils.ColorUtils;
import net.minecraft.client.util.Window;

import java.awt.*;

public class AuthScreen extends SimpleSoarGui {
    private final SimpleAnimation fadeInAnimation = new SimpleAnimation();
    private final SimpleAnimation cardAnimation = new SimpleAnimation();
    private final SimpleAnimation errorAnimation = new SimpleAnimation();

    private AuthState currentState = AuthState.LOGIN;
    private String errorMessage = "";
    private String username = "";
    private String password = "";
    private boolean isProcessing = false;

    // 动画时间常量
    private static final int FADE_IN_DURATION = 25;
    private static final int CARD_ANIMATION_DURATION = 20;
    private static final int ERROR_DURATION = 15;

    private enum AuthState {
        LOGIN, REGISTER
    }

    public AuthScreen() {
        super(false);
    }

    @Override
    public void init() {
        fadeInAnimation.setValue(0);
        cardAnimation.setValue(0);
        errorAnimation.setValue(0);

        fadeInAnimation.onTick(1, FADE_IN_DURATION);
        cardAnimation.onTick(1, CARD_ANIMATION_DURATION);

        // 重置状态
        errorMessage = "";
        isProcessing = false;
    }

    @Override
    public void draw(double mouseX, double mouseY) {
        Window window = client.getWindow();
        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

        // 绘制毛玻璃背景
        drawBlurredBackground(window);

        // 应用淡入效果
        Skia.setAlpha((int) (255 * fadeInAnimation.getValue()));

        // 绘制认证卡片
        drawAuthCard(window, palette, mouseX, mouseY);

        // 绘制错误消息（如果有）
        if (!errorMessage.isEmpty()) {
            drawErrorMessage(window, palette);
        }
    }

    private void drawBlurredBackground(Window window) {
        // 绘制毛玻璃效果
        Skia.drawRoundedBlur(0, 0, window.getWidth(), window.getHeight(), 0);

        // 添加半透明覆盖层
        Skia.drawRect(0, 0, window.getWidth(), window.getHeight(),
            ColorUtils.applyAlpha(new Color(10, 10, 20), 0.6f));
    }

    private void drawAuthCard(Window window, ColorPalette palette, double mouseX, double mouseY) {
        float centerX = window.getWidth() / 2f;
        float centerY = window.getHeight() / 2f;

        // 卡片尺寸
        float cardWidth = 420;
        float cardHeight = 500;
        float cardX = centerX - cardWidth / 2;
        float cardY = centerY - cardHeight / 2;
        float borderRadius = 30;

        // 应用卡片入场动画
        float cardScale = 0.9f + 0.1f * cardAnimation.getValue();
        float cardAlpha = cardAnimation.getValue();

        Skia.save();
        Skia.scale(centerX, centerY, cardScale);
        Skia.setAlpha((int) (255 * cardAlpha));

        // 卡片背景（毛玻璃效果）
        Skia.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, borderRadius,
            ColorUtils.applyAlpha(palette.getSurfaceContainer(), 0.85f));

        // 卡片边框发光效果
        Skia.drawOutline(cardX - 1, cardY - 1, cardWidth + 2, cardHeight + 2,
            borderRadius + 1, 2, ColorUtils.applyAlpha(palette.getPrimary(), 0.3f));

        // 绘制Logo/标题区域
        drawHeaderSection(cardX, cardY, cardWidth, palette);

        // 绘制表单区域
        drawFormSection(cardX, cardY, cardWidth, cardHeight, palette, mouseX, mouseY);

        // 绘制底部切换区域
        drawFooterSection(cardX, cardY, cardWidth, cardHeight, palette, mouseX, mouseY);

        Skia.restore();
    }

    private void drawHeaderSection(float cardX, float cardY, float cardWidth, ColorPalette palette) {
        float headerHeight = 120;

        // 标题背景渐变
        Skia.drawRoundedRect(cardX, cardY, cardWidth, headerHeight, 30, ColorUtils.applyAlpha(palette.getPrimaryContainer(), 0.8f));

        // Logo
        float logoSize = 60;
        float logoX = cardX + 30;
        float logoY = cardY + 30;
        Skia.drawRoundedImage("logo.png", logoX, logoY, logoSize, logoSize, 15);

        // 标题
        String title = currentState == AuthState.LOGIN ? "欢迎回来" : "创建账户";
        Skia.drawText(title, logoX + logoSize + 20, logoY + 25,
            palette.getOnPrimaryContainer(), Fonts.getMedium(24));

        // 副标题
        String subtitle = currentState == AuthState.LOGIN ?
            "登录以继续使用 PupperClient" : "创建新账户开始使用";
        Skia.drawText(subtitle, logoX + logoSize + 20, logoY + 55,
            ColorUtils.applyAlpha(palette.getOnPrimaryContainer(), 0.8f), Fonts.getRegular(14));
    }

    private void drawFormSection(float cardX, float cardY, float cardWidth, float cardHeight,
                                 ColorPalette palette, double mouseX, double mouseY) {
        float formStartY = cardY + 140;
        float inputWidth = cardWidth - 60;
        float inputHeight = 50;
        float inputX = cardX + 30;

        // 用户名输入框
        drawInputField(inputX, formStartY, inputWidth, inputHeight, "用户名", username,
            isFieldHovered(inputX, formStartY, inputWidth, inputHeight, mouseX, mouseY), palette);

        // 密码输入框
        drawInputField(inputX, formStartY + 70, inputWidth, inputHeight, "密码", "•".repeat(password.length()),
            isFieldHovered(inputX, formStartY + 70, inputWidth, inputHeight, mouseX, mouseY), palette);

        // 操作按钮
        drawActionButton(inputX, formStartY + 150, inputWidth, inputHeight, palette, mouseX, mouseY);

        // 处理中状态
        if (isProcessing) {
            drawLoadingIndicator(inputX + inputWidth / 2, formStartY + 150 + inputHeight / 2, palette);
        }
    }

    private void drawInputField(float x, float y, float width, float height, String label,
                                String value, boolean isHovered, ColorPalette palette) {
        float borderRadius = 12;

        // 输入框背景
        Color bgColor = isHovered ? palette.getSurfaceVariant() : palette.getSurface();
        Skia.drawRoundedRect(x, y, width, height, borderRadius,
            ColorUtils.applyAlpha(bgColor, 0.9f));

        // 输入框边框
        Skia.drawOutline(x, y, width, height, borderRadius, 1.5f,
            ColorUtils.applyAlpha(palette.getOutline(), isHovered ? 0.8f : 0.4f));

        // 标签文字
        if (value.isEmpty()) {
            Skia.drawText(label, x + 15, y + height / 2 + 5,
                ColorUtils.applyAlpha(palette.getOnSurfaceVariant(), 0.6f), Fonts.getRegular(16));
        } else {
            Skia.drawText(value, x + 15, y + height / 2 + 5,
                palette.getOnSurface(), Fonts.getRegular(16));
        }

        // 焦点指示器（如果正在输入）
        if (isHovered) {
            Skia.drawOutline(x, y, width, height, borderRadius, 2,
                ColorUtils.applyAlpha(palette.getPrimary(), 0.6f));
        }
    }

    private void drawActionButton(float x, float y, float width, float height,
                                  ColorPalette palette, double mouseX, double mouseY) {
        float borderRadius = 12;
        boolean isHovered = isFieldHovered(x, y, width, height, mouseX, mouseY);

        // 按钮背景（渐变效果）
        Color startColor = palette.getPrimary();
        Color endColor = palette.getPrimaryContainer();

        if (isHovered && !isProcessing) {
            Skia.drawGradientRoundedRect(x, y, width, height, borderRadius, startColor, endColor);
        } else {
            Skia.drawRoundedRect(x, y, width, height, borderRadius,
                isProcessing ? ColorUtils.applyAlpha(palette.getSurfaceVariant(), 0.8f) : startColor);
        }

        // 按钮文字
        String buttonText = isProcessing ? "处理中..." :
            (currentState == AuthState.LOGIN ? "登录" : "注册");

        Skia.drawCenteredText(buttonText, x + width / 2, y + height / 2 + 5,
            isProcessing ? palette.getOnSurfaceVariant() : palette.getOnPrimary(),
            Fonts.getMedium(16));
    }

    private void drawLoadingIndicator(float centerX, float centerY, ColorPalette palette) {
        long time = System.currentTimeMillis();
        float progress = (time % 2000) / 2000f;

        Skia.drawArc(centerX, centerY, 12, 0, progress * 360, 3, palette.getPrimary());
    }

    private void drawFooterSection(float cardX, float cardY, float cardWidth, float cardHeight,
                                   ColorPalette palette, double mouseX, double mouseY) {
        float footerY = cardY + cardHeight - 60;
        String promptText = currentState == AuthState.LOGIN ?
            "还没有账户？" : "已有账户？";
        String actionText = currentState == AuthState.LOGIN ? "立即注册" : "立即登录";

        float textWidth = Skia.getTextWidth(promptText, Fonts.getRegular(14));
        float totalWidth = textWidth + Skia.getTextWidth(actionText, Fonts.getMedium(14)) + 5;
        float startX = cardX + (cardWidth - totalWidth) / 2;

        // 提示文字
        Skia.drawText(promptText, startX, footerY,
            ColorUtils.applyAlpha(palette.getOnSurfaceVariant(), 0.8f), Fonts.getRegular(14));

        // 可点击的操作文字
        float actionX = startX + textWidth + 5;
        boolean isHovered = isFieldHovered(actionX - 5, footerY - 15,
            Skia.getTextWidth(actionText, Fonts.getMedium(14)) + 10, 20, mouseX, mouseY);

        Color actionColor = isHovered ? palette.getPrimary() : palette.getPrimaryContainer();
        Skia.drawText(actionText, actionX, footerY, actionColor, Fonts.getMedium(14));

        // 悬停下划线
        if (isHovered) {
            float underlineWidth = Skia.getTextWidth(actionText, Fonts.getMedium(14));
            Skia.drawRect(actionX, footerY + 2, underlineWidth, 1,
                ColorUtils.applyAlpha(actionColor, 0.8f));
        }
    }

    private void drawErrorMessage(Window window, ColorPalette palette) {
        float centerX = window.getWidth() / 2f;
        float errorY = window.getHeight() / 2f + 250;

        Skia.save();
        Skia.setAlpha((int) (255 * errorAnimation.getValue()));

        // 错误消息背景
        float errorWidth = Skia.getTextWidth(errorMessage, Fonts.getRegular(14)) + 40;
        Skia.drawRoundedRect(centerX - errorWidth / 2, errorY - 20, errorWidth, 40, 20,
            ColorUtils.applyAlpha(new Color(220, 50, 50), 0.9f));

        // 错误消息文字
        Skia.drawCenteredText(errorMessage, centerX, errorY,
            Color.WHITE, Fonts.getRegular(14));

        Skia.restore();
    }

    private boolean isFieldHovered(float x, float y, float width, float height,
                                   double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
            mouseY >= y && mouseY <= y + height;
    }

    private void showError(String message) {
        errorMessage = message;
        errorAnimation.setValue(0);
        errorAnimation.onTick(1, ERROR_DURATION);

        // 3秒后自动隐藏错误消息
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                errorAnimation.onTick(0, ERROR_DURATION);
                Thread.sleep(500);
                errorMessage = "";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void performAuth() {
        if (username.isEmpty() || password.isEmpty()) {
            showError("请填写完整的用户名和密码");
            return;
        }

        if (isProcessing) return;

        isProcessing = true;

        new Thread(() -> {
            try {
                boolean success;

                if (currentState == AuthState.LOGIN) {
                    success = AuthManager.getInstance().login(username, password);
                } else {
                    success = AuthManager.getInstance().register(username, password);
                }

                if (success) {
                    // 认证成功，切换到主界面
                    client.execute(() -> {
                        // 这里可以切换到主菜单或其他界面
                        client.setScreen(null); // 或者你的主菜单界面
                    });
                } else {
                    String error = currentState == AuthState.LOGIN ?
                        "用户名或密码错误" : "用户名已存在或注册失败";
                    showError(error);
                }
            } catch (Exception e) {
                showError("认证过程中出现错误");
                PupperClient.LOGGER.error("Auth error", e);
            } finally {
                isProcessing = false;
            }
        }).start();
    }

    private void toggleAuthState() {
        currentState = currentState == AuthState.LOGIN ? AuthState.REGISTER : AuthState.LOGIN;
        errorMessage = "";
        username = "";
        password = "";
    }

    @Override
    public void mousePressed(double mouseX, double mouseY, int button) {
        if (isProcessing) return;

        Window window = client.getWindow();
        float centerX = window.getWidth() / 2f;
        float cardWidth = 420;
        float cardHeight = 500;
        float cardX = centerX - cardWidth / 2;
        float cardY = window.getHeight() / 2f - cardHeight / 2;

        // 检查操作按钮点击
        float buttonX = cardX + 30;
        float buttonY = cardY + 140 + 150;
        float buttonWidth = cardWidth - 60;
        float buttonHeight = 50;

        if (isFieldHovered(buttonX, buttonY, buttonWidth, buttonHeight, mouseX, mouseY)) {
            performAuth();
            return;
        }

        // 检查状态切换点击
        float footerY = cardY + cardHeight - 60;
        String promptText = currentState == AuthState.LOGIN ? "还没有账户？" : "已有账户？";
        String actionText = currentState == AuthState.LOGIN ? "立即注册" : "立即登录";

        float textWidth = Skia.getTextWidth(promptText, Fonts.getRegular(14));
        float totalWidth = textWidth + Skia.getTextWidth(actionText, Fonts.getMedium(14)) + 5;
        float startX = cardX + (cardWidth - totalWidth) / 2;
        float actionX = startX + textWidth + 5;
        float actionWidth = Skia.getTextWidth(actionText, Fonts.getMedium(14)) + 10;

        if (isFieldHovered(actionX - 5, footerY - 15, actionWidth, 20, mouseX, mouseY)) {
            toggleAuthState();
            return;
        }

        // 检查输入框点击（这里可以扩展为真正的输入处理）
        float inputX = cardX + 30;
        float inputY = cardY + 140;
        float inputWidth = cardWidth - 60;
        float inputHeight = 50;

        // 用户名输入框
        if (isFieldHovered(inputX, inputY, inputWidth, inputHeight, mouseX, mouseY)) {
            // 这里可以打开真正的文本输入
            return;
        }

        // 密码输入框
        if (isFieldHovered(inputX, inputY + 70, inputWidth, inputHeight, mouseX, mouseY)) {
            // 这里可以打开真正的文本输入
            return;
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC键关闭界面
        if (keyCode == 256) { // ESC key
            client.setScreen(null);
        }

        // Enter键提交表单
        if (keyCode == 257 && !isProcessing) { // Enter key
            performAuth();
        }
    }
}
