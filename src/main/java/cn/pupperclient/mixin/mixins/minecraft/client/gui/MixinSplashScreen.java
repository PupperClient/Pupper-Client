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
import org.lwjgl.glfw.GLFW;
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

    // Animation timing variables
    @Unique private long soar_animationStartTime = -1L;
    @Unique private long soar_reloadStartTime = -1L;
    @Unique private static final long MAX_RELOAD_TIME = 15_000L;
    @Unique private static final Identifier CUSTOM_LOGO = Identifier.of("pupper", "logo.png");
    @Unique private static final int LOGO_ACTUAL_SIZE = 1080;
    @Unique private static final float LOGO_SCALE = 0.15f;
    @Unique private static final long ANIMATION_TOTAL_TIME = 4500L;
    @Unique private static final long FADE_DURATION = 500L;
    @Unique private static final long WELCOME_DISPLAY_TIME = 5000L; // 5 second display time
    @Unique private static final long TAP_PROMPT_DELAY = 0;
    @Unique private static final long TTS_CYCLE_DURATION = 2000L; // 2-second cycle for TTS animation
    @Unique private static final long CLICK_FADE_DURATION = 2000L; // 2 second fade out after click
    @Unique private static final int PROGRESS_BAR_HEIGHT = 8;

    // State variables
    @Unique private int lastWindowWidth = -1;
    @Unique private int lastWindowHeight = -1;
    @Unique private boolean skipNextFrame = false;
    @Unique private boolean welcomeDisplayed = false;
    @Unique private boolean tapPromptDisplayed = false;
    @Unique private boolean tapClicked = false;
    @Unique private long welcomeStartTime = -1L;
    @Unique private long tapPromptStartTime = -1L;
    @Unique private long tapClickTime = -1L;

    @Unique
    private void ensureLogoTexture() {
        var tm = this.client.getTextureManager();
        if (tm.getTexture(CUSTOM_LOGO) == null) {
            tm.registerTexture(CUSTOM_LOGO, new ResourceTexture(CUSTOM_LOGO));
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void pupper_takeOverAndRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int width = MinecraftClient.getInstance().getWindow().getWidth();
        int height = MinecraftClient.getInstance().getWindow().getHeight();

        // Recreate surface if window size changed
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
            renderWithSkia(canvas, width, height, mouseX, mouseY);
        });
    }

    @Unique
    private void renderWithSkia(Canvas canvas, int width, int height, int mouseX, int mouseY) {
        ensureLogoTexture();

        // Handle reloading state
        if (this.reloading) {
            if (this.soar_reloadStartTime == -1L) this.soar_reloadStartTime = Util.getMeasuringTimeMs();
            this.soar_animationStartTime = -1L;
            this.welcomeDisplayed = false;
            this.tapPromptDisplayed = false;
            this.tapClicked = false;
            this.welcomeStartTime = -1L;
            this.tapPromptStartTime = -1L;
            this.tapClickTime = -1L;

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

        // Check if welcome screen should be displayed
        if (timePassed >= ANIMATION_TOTAL_TIME && !welcomeDisplayed) {
            welcomeDisplayed = true;
            welcomeStartTime = Util.getMeasuringTimeMs();
            tapPromptDisplayed = false;
            tapClicked = false;
        }

        // Welcome screen logic
        if (welcomeDisplayed) {
            long welcomeTimePassed = Util.getMeasuringTimeMs() - welcomeStartTime;

            // Check if tap prompt should be displayed (1-second delay)
            if (!tapPromptDisplayed) {
                tapPromptDisplayed = true;
                tapPromptStartTime = Util.getMeasuringTimeMs();
            }

            // Check if tap prompt was clicked
            if (!tapClicked && GLFW.glfwGetMouseButton(this.client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                tapClicked = true;
                tapClickTime = Util.getMeasuringTimeMs();
            }

            if (tapClicked) {
                long clickTimePassed = Util.getMeasuringTimeMs() - tapClickTime;

                if (clickTimePassed >= CLICK_FADE_DURATION) {
                    // After fade out, close splash screen
                    try {
                        this.client.setOverlay(null);
                        this.exceptionHandler.accept(Optional.empty());
                    } catch (Exception ignored) {}
                    this.soar_animationStartTime = -1L;
                    this.welcomeDisplayed = false;
                    this.tapPromptDisplayed = false;
                    this.tapClicked = false;
                    this.welcomeStartTime = -1L;
                    this.tapPromptStartTime = -1L;
                    this.tapClickTime = -1L;
                    return;
                }

                renderWelcomeScreen(width, height, welcomeTimePassed, true, clickTimePassed);
                return;
            }

            // Check if timeout (auto exit after display time)
            if (welcomeTimePassed >= WELCOME_DISPLAY_TIME) {
                try {
                    this.client.setOverlay(null);
                    this.exceptionHandler.accept(Optional.empty());
                } catch (Exception ignored) {}
                this.soar_animationStartTime = -1L;
                this.welcomeDisplayed = false;
                this.tapPromptDisplayed = false;
                this.tapClicked = false;
                this.welcomeStartTime = -1L;
                this.tapPromptStartTime = -1L;
                this.tapClickTime = -1L;
                return;
            }

            renderWelcomeScreen(width, height, welcomeTimePassed, false, 0);
            return;
        }

        // Normal loading animation
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
    private boolean isTapPromptClicked(int width, int height, int mouseX, int mouseY) {
        // Calculate tap prompt text position and size
        String tapText = "Tap to start";
        Font tapFont = Fonts.getMedium(24f);
        Rect tapBounds = tapFont.measureText(tapText);

        float tapX = (width - tapBounds.getWidth()) / 2;
        float tapY = height / 2f + 15; // Adjusted to height/2 + 15

        // Check if mouse is within text area (with padding)
        float padding = 15f;
        return mouseX >= tapX - padding &&
            mouseX <= tapX + tapBounds.getWidth() + padding &&
            mouseY >= tapY - padding &&
            mouseY <= tapY + tapBounds.getHeight() + padding;
    }

    @Unique
    private void renderLoadingScreen(int width, int height, long timePassed, float alpha, boolean showProgress) {
        // Background
        Skia.drawRect(0, 0, width, height, new Color(0, 0, 0, (int)(255 * alpha)));

        // Calculate scaled logo size and position
        int scaledSize = (int)(LOGO_ACTUAL_SIZE * LOGO_SCALE);
        int logoX = (width - scaledSize) / 2;
        int logoY = (height - scaledSize) / 3;

        // Draw logo
        drawLogo(logoX, logoY, scaledSize, alpha);

        if (showProgress) {
            // Progress bar
            float progress = Math.min(1f, (float) timePassed / ANIMATION_TOTAL_TIME);
            drawProgressBar(width, height, progress, alpha);
        } else {
            // Reloading progress bar
            drawReloadingProgressBar(width, height, timePassed, alpha);
        }
    }

    @Unique
    private void drawLogo(int x, int y, int size, float alpha) {
        // Draw logo using Skia
        int textureId = MinecraftClient.getInstance().getTextureManager().getTexture(CUSTOM_LOGO).getGlId();
        Skia.drawImage(textureId, x, y, size, size, alpha);
    }

    @Unique
    private void drawProgressBar(int width, int height, float progress, float alpha) {
        int barWidth = width / 2;
        int barHeight = PROGRESS_BAR_HEIGHT;
        int barX = (width - barWidth) / 2;
        int barY = height - 200;

        // Progress bar background
        Skia.drawRect(barX, barY, barWidth, barHeight,
            new Color(0x30, 0x30, 0x30, (int)(255 * alpha)));

        // Progress bar foreground
        int progressWidth = (int)(barWidth * progress);
        Skia.drawRect(barX, barY, progressWidth, barHeight,
            new Color(255, 255, 255, (int)(255 * alpha)));
    }

    @Unique
    private void drawReloadingProgressBar(int width, int height, long time, float alpha) {
        int barWidth = width / 2;
        int barHeight = PROGRESS_BAR_HEIGHT;
        int barX = (width - barWidth) / 2;
        int barY = height - 200;

        // Progress bar background
        Skia.drawRect(barX, barY, barWidth, barHeight,
            new Color(0x30, 0x30, 0x30, (int)(255 * alpha)));

        // Dynamic progress indicator
        long cycle = 1500L;
        float p = (float)(time % cycle) / (float)cycle;
        int indicatorWidth = barWidth / 4;
        int start = (int)((barWidth + indicatorWidth) * p) - indicatorWidth;
        int end = Math.min(start + indicatorWidth, barWidth);

        Skia.drawRect(barX + Math.max(0, start), barY,
            barX + end, barHeight,
            new Color(255, 255, 255, (int)(255 * alpha)));
    }

    @Unique
    private void renderWelcomeScreen(int width, int height, long welcomeTimePassed, boolean clicked, long clickTimePassed) {
        // Background (always full opacity)
        Skia.drawRect(0, 0, width, height, new Color(0, 0, 0, 255));

        // Text transparency calculations
        float welcomeAlpha = 1.0f; // Welcome text always full opacity (no animation)
        float ttsAlpha = 1.0f; // TTS text alpha

        if (clicked) {
            // After click: 2-second fade out for all text
            float clickFadeAlpha = 1f - (float)clickTimePassed / CLICK_FADE_DURATION;
            welcomeAlpha = Math.max(0f, clickFadeAlpha);
            ttsAlpha = Math.max(0f, clickFadeAlpha);
        } else {
            // Before click: TTS text has continuous fade in/out animation
            if (tapPromptDisplayed) {
                long ttsTimePassed = Util.getMeasuringTimeMs() - tapPromptStartTime;
                ttsAlpha = getTTSAnimationAlpha(ttsTimePassed);
            } else {
                ttsAlpha = 0f; // Not displayed yet
            }
            // Welcome text remains at full opacity
            welcomeAlpha = 1.0f;
        }

        // Draw welcome text (no animation, just fade out after click)
        String welcomeText = "Welcome to Pupper Client";
        Font welcomeFont = Fonts.getMedium(32f);

        // Calculate text position (centered)
        Rect welcomeBounds = welcomeFont.measureText(welcomeText);
        float welcomeX = (width - welcomeBounds.getWidth()) / 2;
        float welcomeY = height / 2f - welcomeBounds.getHeight() / 2;

        // Draw welcome text (with transparency only after click)
        Color welcomeColor = new Color(1.0f, 1.0f, 1.0f, welcomeAlpha);
        Skia.drawText(welcomeText, welcomeX, welcomeY, welcomeColor, welcomeFont);

        // Draw TTS text (if displayed)
        if (tapPromptDisplayed) {
            String ttsText = "Tap to start";
            Font ttsFont = Fonts.getMedium(24f);

            // Calculate text position (height/2 + 15)
            Rect ttsBounds = ttsFont.measureText(ttsText);
            float ttsX = (width - ttsBounds.getWidth()) / 2;
            float ttsY = height / 2f + 180;

            // Draw subtle white shadow for TTS text (very light shadow)
            if (ttsAlpha > 0) {
                // White shadow with reduced opacity and smaller offset
                float shadowOpacity = ttsAlpha * 0.3f; // Very subtle shadow
                Color shadowColor = new Color(1.0f, 1.0f, 1.0f, shadowOpacity);

                // Very small offset for subtle shadow
                float shadowOffset = 0.7f;

                // Draw just one offset shadow for subtle effect
                Skia.drawText(ttsText, ttsX + shadowOffset, ttsY, shadowColor, ttsFont);

                // Optional: draw a second layer with even smaller offset for smoother shadow
                float shadowOffset2 = 0.3f;
                Skia.drawText(ttsText, ttsX + shadowOffset2, ttsY, shadowColor, ttsFont);
            }

            // Draw TTS text main layer
            // Use white color for main text to contrast with black background
            Color ttsColor = new Color(1.0f, 1.0f, 1.0f, ttsAlpha);
            Skia.drawText(ttsText, ttsX, ttsY, ttsColor, ttsFont);
        }
    }

    @Unique
    private static float getTTSAnimationAlpha(long ttsTimePassed) {
        // Calculate pulse animation: fade in and out, 2-second cycle
        long cycle = TTS_CYCLE_DURATION;
        float timeInCycle = ttsTimePassed % cycle;

        // Use sine wave for smooth fade in/out animation
        // sin(2π * time / period) mapped to [0.3, 1.0] range
        float sinValue = (float) Math.sin(2 * Math.PI * timeInCycle / cycle);
        float alpha = 0.65f + 0.35f * sinValue; // Varies between 0.3 and 1.0

        // Ensure smooth start
        float fadeInDuration = 500f;
        if (ttsTimePassed < fadeInDuration) {
            alpha *= (float)ttsTimePassed / fadeInDuration;
        }

        return Math.max(0f, Math.min(1f, alpha));
    }
}
