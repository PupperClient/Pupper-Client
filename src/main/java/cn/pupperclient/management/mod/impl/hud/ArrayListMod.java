package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.PupperClient;
import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.Mod;
import cn.pupperclient.management.mod.ModCategory;
import cn.pupperclient.management.mod.api.hud.HUDMod;
import cn.pupperclient.management.mod.event.ModStateChangeEvent;
import cn.pupperclient.management.mod.settings.impl.BooleanSetting;
import cn.pupperclient.management.mod.settings.impl.ComboSetting;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;
import cn.pupperclient.utils.language.I18n;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ArrayListMod extends HUDMod {
    private static ArrayListMod instance;

    // Settings
    private final BooleanSetting backgroundSetting = new BooleanSetting("setting.background",
        "setting.background1.description", Icon.IMAGE, this, true);
    private final BooleanSetting hudSetting = new BooleanSetting("setting.hud",
        "setting.hud.description", Icon.DASHBOARD, this, false);
    private final BooleanSetting renderSetting = new BooleanSetting("setting.render",
        "setting.render.description", Icon.VISIBILITY, this, false);
    private final BooleanSetting playerSetting = new BooleanSetting("setting.player",
        "setting.player.description", Icon.PERSON, this, false);
    private final BooleanSetting otherSetting = new BooleanSetting("setting.other",
        "setting.other.description", Icon.MORE_HORIZ, this, false);
    private final ComboSetting modeSetting = new ComboSetting("setting.mode",
        "setting.mode.description", Icon.ALIGN_HORIZONTAL_RIGHT, this,
        Arrays.asList("setting.right", "setting.left"), "setting.right");

    // Animation constants
    private static final long ANIMATION_DURATION = 300;
    private static final long EXIT_ANIMATION_DURATION = 200;
    private static final float FONT_SIZE = 8.5f;
    private static final float ROW_HEIGHT = 14f;
    private static final float HORIZONTAL_PADDING = 8f;
    private static final float VERTICAL_PADDING = 3f;
    private static final float ITEM_SPACING = 2f;

    // Animation states
    private final Map<String, ModAnimationState> animationStates = new ConcurrentHashMap<>();
    private final List<ModDisplayInfo> sortedDisplayMods = new ArrayList<>();
    private long lastUpdateTime = System.currentTimeMillis();

    public ArrayListMod() {
        super("mod.arraylist.name", "mod.arraylist.description", Icon.LIST);
        instance = this;

        // Listen for mod state changes to trigger animations
        PupperClient.getInstance().getModManager().addStateListener(this::handleModStateChange);
    }

    public static ArrayListMod getInstance() {
        return instance;
    }

    private final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
        draw();
    };

    private void draw() {
        try {
            begin();
            updateAnimationStates();
            drawArrayList();
        } catch (Exception e) {
            PupperClient.LOGGER.error("Error drawing ArrayListMod", e);
            position.setSize(100, 20);
        } finally {
            finish();
        }
    }

    private void updateAnimationStates() {
        long currentTime = System.currentTimeMillis();

        // Get current enabled mods
        List<ModDisplayInfo> enabledMods = getEnabledMods();

        // Update animation states for enabled mods
        for (ModDisplayInfo modInfo : enabledMods) {
            ModAnimationState state = animationStates.get(modInfo.originalName);
            if (state == null) {
                // New mod - start enter animation
                state = new ModAnimationState(modInfo, currentTime, AnimationType.ENTER);
                animationStates.put(modInfo.originalName, state);
            } else if (state.animationType == AnimationType.EXIT) {
                // Mod was exiting but got re-enabled - switch to enter animation
                state.animationType = AnimationType.ENTER;
                state.animationStartTime = currentTime;
                state.modInfo = modInfo;
            } else {
                // Update existing mod info
                state.modInfo = modInfo;
            }
        }

        // Handle mods that are no longer enabled
        Iterator<Map.Entry<String, ModAnimationState>> iterator = animationStates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ModAnimationState> entry = iterator.next();
            String modName = entry.getKey();
            ModAnimationState state = entry.getValue();

            boolean stillEnabled = enabledMods.stream()
                .anyMatch(mod -> mod.originalName.equals(modName));

            if (!stillEnabled && state.animationType != AnimationType.EXIT) {
                // Start exit animation
                state.animationType = AnimationType.EXIT;
                state.animationStartTime = currentTime;
            }

            // Remove mods that have finished exit animation
            if (state.animationType == AnimationType.EXIT) {
                float progress = getAnimationProgress(currentTime, state.animationStartTime, EXIT_ANIMATION_DURATION);
                if (progress >= 1.0f) {
                    iterator.remove();
                }
            }
        }

        // Update sorted display mods
        updateSortedDisplayMods();

        lastUpdateTime = currentTime;
    }

    private void updateSortedDisplayMods() {
        sortedDisplayMods.clear();

        // Add all active mods (both entering and stable)
        for (ModAnimationState state : animationStates.values()) {
            if (state.animationType != AnimationType.EXIT ||
                getAnimationProgress(System.currentTimeMillis(), state.animationStartTime, EXIT_ANIMATION_DURATION) < 1.0f) {
                sortedDisplayMods.add(state.modInfo);
            }
        }

        // Sort by display name length (longest first) for consistent layout
        sortedDisplayMods.sort((a, b) -> Float.compare(b.width, a.width));
    }

    private void drawArrayList() {
        if (sortedDisplayMods.isEmpty()) {
            position.setSize(0, 0);
            return;
        }

        boolean isRightAligned = isRightAligned();
        float maxWidth = calculateMaxWidth();
        float totalHeight = sortedDisplayMods.size() * (ROW_HEIGHT + ITEM_SPACING) - ITEM_SPACING;
        float currentY = 0;

        for (int i = 0; i < sortedDisplayMods.size(); i++) {
            ModDisplayInfo modInfo = sortedDisplayMods.get(i);
            ModAnimationState state = animationStates.get(modInfo.originalName);

            if (state != null) {
                drawModEntry(modInfo, state, maxWidth, currentY, i, isRightAligned);
            }

            currentY += ROW_HEIGHT + ITEM_SPACING;
        }

        position.setSize(maxWidth, totalHeight);
    }

    private void drawModEntry(ModDisplayInfo modInfo, ModAnimationState state, float maxWidth,
                              float y, int index, boolean isRightAligned) {
        long currentTime = System.currentTimeMillis();
        long animationDuration = state.animationType == AnimationType.EXIT ?
            EXIT_ANIMATION_DURATION : ANIMATION_DURATION;

        float progress = getAnimationProgress(currentTime, state.animationStartTime, animationDuration);

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

        // Calculate position
        float bgWidth = modInfo.width + HORIZONTAL_PADDING * 2;
        float bgX = getX() + (isRightAligned ? (maxWidth - bgWidth) : 0) + animationOffset;
        float bgY = getY() + y;

        // Draw background with alpha
        if (backgroundSetting.isEnabled()) {
            drawRoundedBackground(bgX, bgY, bgWidth, (int)alpha);
        }

        // Draw text with alpha
        Color textColor = new Color(255, 255, 255, (int)alpha);
        float textX = bgX + HORIZONTAL_PADDING;
        float textY = bgY + VERTICAL_PADDING;

        Skia.drawText(modInfo.displayName, textX, textY, textColor, Fonts.getRegular(FONT_SIZE));
    }

    private void drawRoundedBackground(float x, float y, float width, int alpha) {
        float radius = ROW_HEIGHT / 2;
        // 使用带透明度的背景
        Skia.drawRoundedRect(x, y, width, ROW_HEIGHT, radius,
            new Color(255, 255, 255, Math.min(120, alpha)));
    }

    private List<ModDisplayInfo> getEnabledMods() {
        List<ModDisplayInfo> enabledMods = new ArrayList<>();

        for (Mod mod : PupperClient.getInstance().getModManager().getMods()) {
            if (shouldDisplayMod(mod) && mod.isEnabled() && !mod.isHidden()) {
                String displayName = I18n.get(mod.getName());
                float nameWidth = Skia.getTextBounds(displayName, Fonts.getRegular(FONT_SIZE)).getWidth();
                enabledMods.add(new ModDisplayInfo(mod.getName(), displayName, nameWidth));
            }
        }

        return enabledMods;
    }

    private boolean shouldDisplayMod(Mod mod) {
        ModCategory category = mod.getCategory();

        if (category == ModCategory.HUD && !hudSetting.isEnabled()) {
            return false;
        }
        if (category == ModCategory.RENDER && !renderSetting.isEnabled()) {
            return false;
        }
        if (category == ModCategory.PLAYER && !playerSetting.isEnabled()) {
            return false;
        }
        if (category == ModCategory.MISC && !otherSetting.isEnabled()) {
            return false;
        }

        // Always display HACK and FUN category mods if their respective settings are enabled
        return true;
    }

    private float calculateMaxWidth() {
        float maxWidth = 0;
        for (ModDisplayInfo modInfo : sortedDisplayMods) {
            float totalWidth = modInfo.width + HORIZONTAL_PADDING * 2;
            if (totalWidth > maxWidth) {
                maxWidth = totalWidth;
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

    private void handleModStateChange(ModStateChangeEvent event) {
        // Trigger animation update on next render
        lastUpdateTime = System.currentTimeMillis() - 1;
    }

    @Override
    public float getRadius() {
        return 6;
    }

    // Helper classes
    private static class ModDisplayInfo {
        String originalName;
        String displayName;
        float width;

        ModDisplayInfo(String originalName, String displayName, float width) {
            this.originalName = originalName;
            this.displayName = displayName;
            this.width = width;
        }
    }

    private static class ModAnimationState {
        ModDisplayInfo modInfo;
        long animationStartTime;
        AnimationType animationType;

        ModAnimationState(ModDisplayInfo modInfo, long animationStartTime, AnimationType animationType) {
            this.modInfo = modInfo;
            this.animationStartTime = animationStartTime;
            this.animationType = animationType;
        }
    }

    private enum AnimationType {
        ENTER,
        EXIT
    }
}
