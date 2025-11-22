package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.PupperClient;
import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.HUDMod;
import cn.pupperclient.management.mod.settings.impl.BooleanSetting;
import cn.pupperclient.management.mod.settings.impl.ComboSetting;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cn.pupperclient.management.mod.impl.hud.ArrayListMod.ICON_TEXT_SPACING;

public class PotionHudMod extends HUDMod {
    private static PotionHudMod instance;

    // Settings
    private final BooleanSetting backgroundSetting = new BooleanSetting("setting.background",
        "setting.background1.description", Icon.IMAGE, this, true);
    private final ComboSetting modeSetting = new ComboSetting("setting.mode",
        "setting.mode.description", Icon.ALIGN_HORIZONTAL_RIGHT, this,
        Arrays.asList("setting.right", "setting.left"), "setting.right");

    // Design constants
    private static final float FONT_SIZE = 8.5f;
    private static final float ROW_HEIGHT = 14f;
    private static final float HORIZONTAL_PADDING = 6f;
    private static final float VERTICAL_PADDING = 3f;
    private static final float ITEM_SPACING = 2f;
    private static final float EFFECT_TIME_SPACING = 4f; // 效果名称和时间之间的间距

    // Animation states
    private final Map<String, PotionAnimationState> animationStates = new ConcurrentHashMap<>();
    private final List<PotionDisplayInfo> sortedDisplayPotions = new ArrayList<>();
    private long lastUpdateTime = System.currentTimeMillis();

    public PotionHudMod() {
        super("mod.potionhud.name", "mod.potionhud.description", Icon.LIST);
        instance = this;
    }

    public static PotionHudMod getInstance() {
        return instance;
    }

    private final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
        draw();
    };

    private void draw() {
        try {
            begin();
            updateAnimationStates();
            drawPotionHud();
        } catch (Exception e) {
            PupperClient.LOGGER.error("Error drawing PotionHudMod", e);
            position.setSize(100, 20);
        } finally {
            finish();
        }
    }

    private void updateAnimationStates() {
        long currentTime = System.currentTimeMillis();

        // Get current active potion effects
        List<PotionDisplayInfo> activePotions = getActivePotions();

        // Update animation states for active potions
        for (PotionDisplayInfo potionInfo : activePotions) {
            PotionAnimationState state = animationStates.get(potionInfo.effectId);
            if (state == null) {
                // New effect - start enter animation
                state = new PotionAnimationState(potionInfo, currentTime, AnimationType.ENTER);
                animationStates.put(potionInfo.effectId, state);
            } else if (state.animationType == AnimationType.EXIT) {
                // Effect was exiting but got re-applied - switch to enter animation
                state.animationType = AnimationType.ENTER;
                state.animationStartTime = currentTime;
                state.potionInfo = potionInfo;
            } else {
                // Update existing effect info
                state.potionInfo = potionInfo;
            }
        }

        // Handle effects that are no longer active
        Iterator<Map.Entry<String, PotionAnimationState>> iterator = animationStates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PotionAnimationState> entry = iterator.next();
            String effectId = entry.getKey();
            PotionAnimationState state = entry.getValue();

            boolean stillActive = activePotions.stream()
                .anyMatch(potion -> potion.effectId.equals(effectId));

            if (!stillActive && state.animationType != AnimationType.EXIT) {
                // Start exit animation
                state.animationType = AnimationType.EXIT;
                state.animationStartTime = currentTime;
            }

            // Remove effects that have finished exit animation
            if (state.animationType == AnimationType.EXIT) {
                float progress = getAnimationProgress(currentTime, state.animationStartTime, 200);
                if (progress >= 1.0f) {
                    iterator.remove();
                }
            }
        }

        // Update sorted display potions
        updateSortedDisplayPotions();

        lastUpdateTime = currentTime;
    }

    private void updateSortedDisplayPotions() {
        sortedDisplayPotions.clear();

        // Add all active effects (both entering and stable)
        for (PotionAnimationState state : animationStates.values()) {
            if (state.animationType != AnimationType.EXIT ||
                getAnimationProgress(System.currentTimeMillis(), state.animationStartTime, 200) < 1.0f) {
                sortedDisplayPotions.add(state.potionInfo);
            }
        }

        // Sort by display name length (longest first) for consistent layout
        sortedDisplayPotions.sort((a, b) -> Float.compare(b.totalWidth, a.totalWidth));
    }

    private void drawPotionHud() {
        boolean isRightAligned = isRightAligned();

        // Calculate total height including title and all effect entries
        int totalEntries = 1 + sortedDisplayPotions.size(); // 1 for title + effect entries
        float totalHeight = totalEntries * (ROW_HEIGHT + ITEM_SPACING) - ITEM_SPACING;

        // Calculate max width
        float maxWidth = calculateMaxWidth();

        float currentY = 0;

        // Draw title "Active Potions" (always visible)
        drawTitleEntry(maxWidth, currentY, isRightAligned);
        currentY += ROW_HEIGHT + ITEM_SPACING;

        // Draw active potion effects
        for (int i = 0; i < sortedDisplayPotions.size(); i++) {
            PotionDisplayInfo potionInfo = sortedDisplayPotions.get(i);
            PotionAnimationState state = animationStates.get(potionInfo.effectId);

            if (state != null) {
                drawPotionEntry(potionInfo, state, maxWidth, currentY, i, isRightAligned);
            }

            currentY += ROW_HEIGHT + ITEM_SPACING;
        }

        position.setSize(maxWidth, totalHeight);
    }

    private void drawTitleEntry(float maxWidth, float y, boolean isRightAligned) {
        String titleIcon = Icon.SCIENCE;
        String titleText = "Active Potions";

        // Calculate widths
        float iconWidth = Skia.getTextBounds(titleIcon, Fonts.getRegular(FONT_SIZE)).getWidth();
        float textWidth = Skia.getTextBounds(titleText, Fonts.getRegular(FONT_SIZE)).getWidth();

        // Calculate background dimensions
        float textBgWidth = textWidth + HORIZONTAL_PADDING * 2;
        float iconBgWidth = iconWidth + HORIZONTAL_PADDING * 2;
        float totalWidth = iconBgWidth + ICON_TEXT_SPACING + textBgWidth;

        // Calculate positions
        float bgX = getX() + (isRightAligned ? (maxWidth - textBgWidth) : 0);
        float bgY = getY() + y;

        // Draw backgrounds
        if (backgroundSetting.isEnabled()) {
            // Draw icon background
            drawRoundedBackground(bgX, bgY, iconBgWidth, 255);

            // Draw text background
            float textBgX = bgX + iconBgWidth + ICON_TEXT_SPACING;
            drawRoundedBackground(textBgX, bgY, textBgWidth, 255);
        }

        // Draw title icon and text
        float iconX = bgX + HORIZONTAL_PADDING;
        float textX = bgX + iconBgWidth + ICON_TEXT_SPACING + HORIZONTAL_PADDING;
        float contentY = bgY + VERTICAL_PADDING;

        Skia.drawText(titleIcon, iconX, contentY, Color.WHITE, Fonts.getIcon(9.25F));
        Skia.drawText(titleText, textX, contentY, Color.WHITE, Fonts.getRegular(FONT_SIZE));
    }

    private void drawPotionEntry(PotionDisplayInfo potionInfo, PotionAnimationState state, float maxWidth,
                                 float y, int index, boolean isRightAligned) {
        long currentTime = System.currentTimeMillis();
        float progress = getAnimationProgress(currentTime, state.animationStartTime, 300);

        // Apply easing
        float easedProgress = easeOutCubic(progress);

        // For exit animations, we want to reverse the progress
        if (state.animationType == AnimationType.EXIT) {
            easedProgress = 1.0f - easedProgress;
        }

        // Calculate animation properties based on alignment
        float animationOffset;
        float alpha = 255 * easedProgress;

        if (isRightAligned) {
            // Right-aligned: animate from right to left
            animationOffset = maxWidth * (1 - easedProgress);
        } else {
            // Left-aligned: animate from left to right
            animationOffset = -maxWidth * (1 - easedProgress);
        }

        // Calculate positions
        float totalWidth = potionInfo.effectBgWidth + EFFECT_TIME_SPACING + potionInfo.timeBgWidth;
        float bgX = getX() + (isRightAligned ? (maxWidth - totalWidth) : 0) + animationOffset;
        float bgY = getY() + y;

        // Draw backgrounds with alpha
        if (backgroundSetting.isEnabled()) {
            // Draw effect name background
            drawRoundedBackground(bgX, bgY, potionInfo.effectBgWidth, (int)alpha);

            // Draw time background
            float timeBgX = bgX + potionInfo.effectBgWidth + EFFECT_TIME_SPACING;
            drawRoundedBackground(timeBgX, bgY, potionInfo.timeBgWidth, (int)alpha);
        }

        // Draw content with alpha
        Color contentColor = new Color(255, 255, 255, (int)alpha);

        // Draw effect name and time
        float effectX = bgX + HORIZONTAL_PADDING;
        float timeX = bgX + potionInfo.effectBgWidth + EFFECT_TIME_SPACING + HORIZONTAL_PADDING;
        float contentY = bgY + VERTICAL_PADDING;

        Skia.drawText(potionInfo.effectName, effectX, contentY, contentColor, Fonts.getRegular(FONT_SIZE));
        Skia.drawText(potionInfo.timeText, timeX, contentY, contentColor, Fonts.getRegular(FONT_SIZE));
    }

    private void drawRoundedBackground(float x, float y, float width, int alpha) {
        float radius = ROW_HEIGHT / 2;
        Skia.drawRoundedRect(x, y, width, ROW_HEIGHT, radius,
            new Color(255, 255, 255, Math.min(120, alpha)));
    }

    private List<PotionDisplayInfo> getActivePotions() {
        List<PotionDisplayInfo> activePotions = new ArrayList<>();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return activePotions;

        // Get all active status effects
        Collection<StatusEffectInstance> effects = client.player.getStatusEffects();

        for (StatusEffectInstance effect : effects) {
            RegistryEntry <StatusEffect> statusEffect = effect.getEffectType();
            String effectkey = statusEffect.value().getTranslationKey();
            String effectName = statusEffect.value().getName().getString();
            String timeText = formatDuration(effect);

            // Calculate widths for layout
            float effectWidth = Skia.getTextBounds(effectName, Fonts.getRegular(FONT_SIZE)).getWidth();
            float timeWidth = Skia.getTextBounds(timeText, Fonts.getRegular(FONT_SIZE)).getWidth();

            // Calculate background widths
            float effectBgWidth = effectWidth + HORIZONTAL_PADDING * 2;
            float timeBgWidth = timeWidth + HORIZONTAL_PADDING * 2;
            float totalWidth = effectBgWidth + EFFECT_TIME_SPACING + timeBgWidth;

            // String effectId = Registries.STATUS_EFFECT.getId(statusEffect.value()).toString();

            activePotions.add(new PotionDisplayInfo(effectkey, effectName, timeText,
                totalWidth, effectBgWidth, timeBgWidth));
        }

        return activePotions;
    }

    private String formatDuration(StatusEffectInstance effect) {
        if (effect.isInfinite()) {
            return "inf";
        }

        int duration = effect.getDuration();
        int seconds = duration / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private float calculateMaxWidth() {
        float maxWidth = 0;

        // Calculate title width
        String titleText = "Active Potions";
        float titleWidth = Skia.getTextBounds(titleText, Fonts.getRegular(FONT_SIZE)).getWidth();
        float titleBgWidth = titleWidth + HORIZONTAL_PADDING * 2;
        maxWidth = Math.max(maxWidth, titleBgWidth);

        // Calculate max width from active potions
        for (PotionDisplayInfo potionInfo : sortedDisplayPotions) {
            if (potionInfo.totalWidth > maxWidth) {
                maxWidth = potionInfo.totalWidth;
            }
        }
        return maxWidth;
    }

    private boolean isRightAligned() {
        return "setting.right".equals(modeSetting.getOption());
    }

    private float getAnimationProgress(long currentTime, long startTime, long duration) {
        long elapsed = currentTime - startTime;
        return Math.min(elapsed / (float) duration, 1.0f);
    }

    // Easing functions
    private float easeOutCubic(float x) {
        return (float) (1 - Math.pow(1 - x, 3));
    }

    @Override
    public float getRadius() {
        return 6;
    }

    private static class PotionDisplayInfo {
        String effectId;
        String effectName;
        String timeText;
        float totalWidth; // Total width including both backgrounds and spacing
        float effectBgWidth;  // Effect name background width
        float timeBgWidth;  // Time background width

        PotionDisplayInfo(String effectId, String effectName, String timeText,
                          float totalWidth, float effectBgWidth, float timeBgWidth) {
            this.effectId = effectId;
            this.effectName = effectName;
            this.timeText = timeText;
            this.totalWidth = totalWidth;
            this.effectBgWidth = effectBgWidth;
            this.timeBgWidth = timeBgWidth;
        }
    }

    private static class PotionAnimationState {
        PotionDisplayInfo potionInfo;
        long animationStartTime;
        AnimationType animationType;

        PotionAnimationState(PotionDisplayInfo potionInfo, long animationStartTime, AnimationType animationType) {
            this.potionInfo = potionInfo;
            this.animationStartTime = animationStartTime;
            this.animationType = animationType;
        }
    }

    private enum AnimationType {
        ENTER,
        EXIT
    }
}
