package cn.pupperclient.mixin.mixins.minecraft.client.gui;

import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.context.SkiaContext;
import cn.pupperclient.skia.font.Fonts;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.types.Rect;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(SplashOverlay.class)
public abstract class MixinSplashScreen {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private boolean reloading;
    @Shadow @Final private Consumer<Optional<Throwable>> exceptionHandler;
    @Unique private long soar_animationStartTime = -1L;
    @Unique private long soar_reloadStartTime = -1L;
    @Unique private static final long MAX_RELOAD_TIME = 15_000L;
    @Unique private static final Identifier CUSTOM_LOGO = Identifier.of("pupper", "logo.png");
    @Unique private static final int LOGO_ACTUAL_SIZE = 1080;
    @Unique private static final float LOGO_SCALE = 0.15f;
    @Unique private static final long ANIMATION_TOTAL_TIME = 4500L;
    @Unique private static final long FADE_DURATION = 500L;
    @Unique private static final long WELCOME_DISPLAY_TIME = 2000L;
    @Unique private static final int PROGRESS_BAR_HEIGHT = 4;
    @Unique private int lastWindowWidth = -1;
    @Unique private int lastWindowHeight = -1;
    @Unique private boolean skipNextFrame = false;
    @Unique private boolean welcomeDisplayed = false;
    @Unique private long welcomeStartTime = -1L;

    @Unique
    private void ensureLogoTexture() {
        var tm = this.client.getTextureManager();
        if (tm.getTexture(CUSTOM_LOGO) == null) {
            tm.registerTexture(CUSTOM_LOGO, new ResourceTexture(CUSTOM_LOGO));
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void pupper_takeOverAndRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//        int width = context.getScaledWindowWidth();
//        int height = context.getScaledWindowHeight();
        int width = MinecraftClient.getInstance().getWindow().getWidth();
        int height = MinecraftClient.getInstance().getWindow().getHeight();

        if (lastWindowWidth != width || lastWindowHeight != height) {
            SkiaContext.createSurface(width, height);
            lastWindowWidth = width;
            lastWindowHeight = height;
        }

        if (width <= 0 || height <= 0) {
            return;
        }

        ci.cancel();

        SkiaContext.draw(canvas -> {
            renderWithSkia(canvas, width, height);
        });
    }

    @Unique
    private void renderWithSkia(Canvas canvas, int width, int height) {
        ensureLogoTexture();

        if (this.reloading) {
            if (this.soar_reloadStartTime == -1L) this.soar_reloadStartTime = Util.getMeasuringTimeMs();
            this.soar_animationStartTime = -1L;
            this.welcomeDisplayed = false;
            this.welcomeStartTime = -1L;

            long reloadElapsed = Util.getMeasuringTimeMs() - this.soar_reloadStartTime;
            if (reloadElapsed > MAX_RELOAD_TIME) {
                try {
                    this.client.setOverlay(null);
                    this.exceptionHandler.accept(Optional.empty());
                } catch (Exception ignored) {}
                this.soar_reloadStartTime = -1L;
                return;
            }

            renderLoadingScreen(width, height, reloadElapsed, 1.0f, false);
            return;
        }

        this.soar_reloadStartTime = -1L;
        if (this.soar_animationStartTime == -1L) {
            this.soar_animationStartTime = Util.getMeasuringTimeMs();
        }

        long timePassed = Util.getMeasuringTimeMs() - this.soar_animationStartTime;

        // 检查是否应该显示欢迎文字
        if (timePassed >= ANIMATION_TOTAL_TIME && !welcomeDisplayed) {
            welcomeDisplayed = true;
            welcomeStartTime = Util.getMeasuringTimeMs();
        }

        // 欢迎文字显示逻辑
        if (welcomeDisplayed) {
            long welcomeTimePassed = Util.getMeasuringTimeMs() - welcomeStartTime;

            if (welcomeTimePassed >= WELCOME_DISPLAY_TIME) {
                try {
                    this.client.setOverlay(null);
                    this.exceptionHandler.accept(Optional.empty());
                } catch (Exception ignored) {}
                this.soar_animationStartTime = -1L;
                this.welcomeDisplayed = false;
                this.welcomeStartTime = -1L;
                return;
            }

            renderWelcomeScreen(width, height, welcomeTimePassed);
            return;
        }

        // 正常加载动画
        float alpha = 1f;
        long fadeStartTime = ANIMATION_TOTAL_TIME - FADE_DURATION;
        if (timePassed > fadeStartTime) {
            long fadeTimePassed = timePassed - fadeStartTime;
            alpha = 1f - (float)fadeTimePassed / FADE_DURATION;
        }
        alpha = Math.max(0f, alpha);

        renderLoadingScreen(width, height, timePassed, alpha, true);
    }

    @Unique
    private void renderLoadingScreen(int width, int height, long timePassed, float alpha, boolean showProgress) {
        // 背景
        Skia.drawRect(0, 0, width, height, new Color(0, 0, 0, (int)(255 * alpha)));

        // 计算缩放后的Logo尺寸和位置
        int scaledSize = (int)(LOGO_ACTUAL_SIZE * LOGO_SCALE);
        int logoX = (width - scaledSize) / 2;
        int logoY = (height - scaledSize) / 3;

        // 绘制Logo
        drawLogo(logoX, logoY, scaledSize, alpha);

//        // 绘制loading圈 - 在Logo下方
//        float circleCenterX = width / 2f;
//        float circleCenterY = logoY + scaledSize + 50;
//        drawLoadingCircle(circleCenterX, circleCenterY, timePassed, alpha);

        if (showProgress) {
            // 进度条 - 在loading圈下方
            float progress = Math.min(1f, (float) timePassed / ANIMATION_TOTAL_TIME);
            drawProgressBar(width, height, progress, alpha);
        } else {
            // 重载时的动态进度条
            drawReloadingProgressBar(width, height, timePassed, alpha);
        }
    }

    @Unique
    private void drawLogo(int x, int y, int size, float alpha) {
        // 使用Skia绘制Logo
        int textureId = MinecraftClient.getInstance().getTextureManager().getTexture(CUSTOM_LOGO).getGlId();
        Skia.drawImage(textureId, x, y, size, size, alpha);
    }

    @Unique
    private void drawLoadingCircle(float centerX, float centerY, long time, float alpha) {
        float radius = 20f;
        float strokeWidth = 4f;

        // 计算旋转角度（每1.5秒旋转一圈）
        float rotation = (time % 1500) / 1500f * 360f;

        // 绘制loading圈背景
        Skia.drawCircle(centerX, centerY, radius,
            new Color(60, 60, 60, (int)(150 * alpha)));

        // 绘制旋转的圆弧
        Skia.drawArc(centerX, centerY, radius, rotation, 270f, strokeWidth,
            new Color(255, 255, 255, (int)(200 * alpha)));
    }

    @Unique
    private void drawProgressBar(int width, int height, float progress, float alpha) {
        int barWidth = width / 2;
        int barHeight = PROGRESS_BAR_HEIGHT;
        int barX = (width - barWidth) / 2;
        int barY = height - 80; // 从底部向上偏移

        // 进度条背景
        Skia.drawRect(barX, barY, barWidth, barHeight,
            new Color(0x30, 0x30, 0x30, (int)(255 * alpha)));

        // 进度条前景
        int progressWidth = (int)(barWidth * progress);
        Skia.drawRect(barX, barY, progressWidth, barHeight,
            new Color(255, 255, 255, (int)(255 * alpha)));
    }

    @Unique
    private void drawReloadingProgressBar(int width, int height, long time, float alpha) {
        int barWidth = width / 2;
        int barHeight = PROGRESS_BAR_HEIGHT;
        int barX = (width - barWidth) / 2;
        int barY = height - 80;

        // 进度条背景
        Skia.drawRect(barX, barY, barWidth, barHeight,
            new Color(0x30, 0x30, 0x30, (int)(255 * alpha)));

        // 动态进度指示器
        long cycle = 1500L;
        float p = (float)(time % cycle) / (float)cycle;
        int indicatorWidth = barWidth / 4;
        int start = (int)((barWidth + indicatorWidth) * p) - indicatorWidth;
        int end = Math.min(start + indicatorWidth, barWidth);

        Skia.drawRect(barX + Math.max(0, start), barY,
            barX + end, barY + barHeight,
            new Color(255, 255, 255, (int)(255 * alpha)));
    }

    @Unique
    private void renderWelcomeScreen(int width, int height, long welcomeTimePassed) {
        // 背景
        float bgAlpha = getWelcomeAlpha(welcomeTimePassed);
        Skia.drawRect(0, 0, width, height, new Color(0, 0, 0, (int)(255 * bgAlpha)));

        // 计算欢迎文字的透明度
        float textAlpha = getWelcomeAlpha(welcomeTimePassed);

        // 绘制欢迎文字
        String welcomeText = "Welcome to Pupper Client";
        Font font = Fonts.getMedium(32f);

        // 计算文字位置（居中）
        Rect textBounds = font.measureText(welcomeText);
        float textX = (width - textBounds.getWidth()) / 2;
        float textY = height / 2f - textBounds.getHeight() / 2;

        // 绘制文字（带透明度）
        Color welcomeColor = new Color(1.0f, 1.0f, 1.0f, textAlpha);
        Skia.drawText(welcomeText, textX, textY, welcomeColor, font);
    }

    @Unique
    private static float getWelcomeAlpha(long welcomeTimePassed) {
        float welcomeAlpha = 1.0f;
        long fadeDuration = 500L;

        if (welcomeTimePassed < fadeDuration) {
            // 渐入
            welcomeAlpha = (float) welcomeTimePassed / fadeDuration;
        } else if (welcomeTimePassed > WELCOME_DISPLAY_TIME - fadeDuration) {
            // 渐出
            long fadeOutTime = welcomeTimePassed - (WELCOME_DISPLAY_TIME - fadeDuration);
            welcomeAlpha = 1.0f - (float) fadeOutTime / fadeDuration;
        }

        welcomeAlpha = Math.max(0f, Math.min(1f, welcomeAlpha));
        return welcomeAlpha;
    }
}
