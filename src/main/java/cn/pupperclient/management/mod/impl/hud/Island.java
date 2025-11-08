package cn.pupperclient.management.mod.impl.hud;

import cn.pupperclient.Soar;
import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.event.mod.AutoAgainEvent;
import cn.pupperclient.management.mod.api.hud.HUDMod;
import cn.pupperclient.management.mod.event.ModStateChangeEvent;
import cn.pupperclient.management.mod.settings.impl.StringSetting;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;
import cn.pupperclient.utils.language.I18n;
import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.types.Rect;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class Island extends HUDMod {
    private final StringSetting name = new StringSetting("mod.Island.customname", "mod.Island.customname.description", Icon.FLIGHT_LAND, this, "Soar CN");

    private static final long DISPLAY_DURATION = 2000;
    private static final long TRANSITION_DURATION = 400;
    private static final long NORMAL_TEXT_DURATION = 5000;
    private static final long SMTC_TEXT_DURATION = 3000;

    private static boolean isConfigLoading = false;
    private static boolean isAutoAgain = false;

    // Mod状态队列
    private final Queue<ModStateEvent> modStateQueue = new LinkedList<>();
    private ModStateEvent currentModState = null;
    private long modStateStartTime = 0;

    // 文本循环
    private long lastTextSwitchTime = System.currentTimeMillis();
    private boolean showingSmtcText = false;

    // 动画进度
    private float expandProgress = 0.0f;
    private float textAlpha = 0.0f;

    // 动画状态
    private boolean isEntering = false;
    private boolean isExiting = false;

    public Island() {
        super("mod.Island.name", "mod.Island.description", Icon.FLIGHT_LAND);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Soar.getInstance().getModManager().addStateListener(this::handleModStateChange);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Soar.getInstance().getModManager().removeStateListener(this::handleModStateChange);
    }

    // 静态方法：在配置加载前调用
    public static void setConfigLoading(boolean loading) {
        isConfigLoading = loading;
    }

    public EventBus.EventListener<RenderSkiaEvent> onRenderSkia = this::draw;

    @Override
    public String getIcon() {
        return "";
    }

    @EventListener
    public void onHeypixelAgain(AutoAgainEvent e) {
        isAutoAgain = true;
    }

    protected void draw(RenderSkiaEvent event) {
        updateAnimationState();
        updateTextCycle();

        float fontSize = 9;
        float iconSize = 10.5F;
        float padding = 5;
        boolean hasIcon = getIcon() != null;

        // 基础高度
        float baseHeight = fontSize + (padding * 2);
        // 扩展高度（用于ModStateText）
        float expandedHeight = baseHeight * 2;

        // 当前高度根据动画进度计算
        float height = baseHeight + (expandedHeight - baseHeight) * expandProgress;

        // 计算文本宽度
        float textWidth = calculateTextWidth(fontSize, iconSize);
        Rect iconBounds = Skia.getTextBounds(getIcon(), Fonts.getIcon(iconSize));
        float width = textWidth + (padding * 2) + (hasIcon ? iconBounds.getWidth() + 4 : 0);

        FontMetrics metrics = Fonts.getRegular(fontSize).getMetrics();
        float textCenterY = (metrics.getAscent() - metrics.getDescent()) / 2 - metrics.getAscent();
        float normalTextY = getY() + (baseHeight / 2) - textCenterY;

        this.begin();
        this.drawBackground(getX(), getY(), width, height);

        if (hasIcon) {
            this.drawText(getIcon(), getX() + padding, getY() + (height / 2) - (iconBounds.getHeight() / 2),
                Fonts.getIcon(iconSize + 5F));
        }

        float textX = getX() + padding + (hasIcon ? iconBounds.getWidth() + 4 : 0);

        if (isShowingModState()) {
            // 显示ModStateText
            drawModStateText(textX, normalTextY, fontSize, iconSize);
        } else if (isAutoAgain) {

        } else {
            drawNormalTextWithIcons(textX, normalTextY, fontSize, iconSize, 1.0f);
        }

        this.finish();
        position.setSize(width, height);
    }

    private void drawModStateText(float textX, float textY, float fontSize, float iconSize) {
        if (currentModState == null) return;

        // 计算ModStateText的位置（在扩展区域中央）
        float modStateTextY = textY + (position.getHeight() - (fontSize + 10)) / 2;

        // 绘制标题
        drawTextWithAlpha("Module Toggle", textX, modStateTextY - fontSize - 2,
            Fonts.getRegular(fontSize), textAlpha);

        // 绘制状态图标和文本
        String stateIcon = currentModState.enabled ? Icon.CHECK : Icon.CLOSE;
        drawTextWithAlpha(stateIcon, textX, modStateTextY + 4, Fonts.getIcon(iconSize), textAlpha);

        String modStateText = getModStateText(currentModState);
        drawTextWithAlpha(modStateText, textX + Skia.getTextBounds(stateIcon, Fonts.getIcon(iconSize)).getWidth() + 2,
            modStateTextY + 4, Fonts.getRegular(fontSize), textAlpha);
    }

    private void drawNormalTextWithIcons(float startX, float startY, float fontSize, float iconSize, float alpha) {
        float currentX = startX;

        Skia.drawImage("logo.png", currentX - 6, startY - 4.5F, iconSize + 6, iconSize + 6);
        currentX += iconSize + 2;

        drawTextWithAlpha(name.getValue() + " · ", currentX, startY, Fonts.getRegular(fontSize), alpha);
        currentX += Skia.getTextBounds(name.getValue() + " · ", Fonts.getRegular(fontSize)).getWidth() + 5;

        // 玩家图标和名称
        String playerIcon = Icon.PERSON;
        drawTextWithAlpha(playerIcon, currentX, startY, Fonts.getIcon(iconSize), alpha);
        currentX += Skia.getTextBounds(playerIcon, Fonts.getIcon(iconSize)).getWidth() + 2;

        String playerName = mc.player != null ? mc.player.getName().getString() : "NULL";
        drawTextWithAlpha(playerName + " · ", currentX, startY, Fonts.getRegular(fontSize), alpha);
        currentX += Skia.getTextBounds(playerName + " · ", Fonts.getRegular(fontSize)).getWidth() + 5;

        // 服务器信息和延迟
        String linkIcon = Icon.LINK;
        drawTextWithAlpha(linkIcon, currentX, startY + 1, Fonts.getIcon(iconSize), alpha);
        currentX += Skia.getTextBounds(linkIcon, Fonts.getIcon(iconSize)).getWidth() + 2;

        String serverInfo = getServerInfo();
        drawTextWithAlpha(serverInfo + " · ", currentX, startY, Fonts.getRegular(fontSize), alpha);
        currentX += Skia.getTextBounds(serverInfo + " · ", Fonts.getRegular(fontSize)).getWidth() + 5;

        // FPS图标和数值
        String fpsIcon = Icon.DESKTOP_WINDOWS;
        drawTextWithAlpha(fpsIcon, currentX, startY - 1F, Fonts.getIcon(iconSize), alpha);
        currentX += Skia.getTextBounds(fpsIcon, Fonts.getIcon(iconSize)).getWidth() + 2;

        String fpsText = mc.getCurrentFps() + " FPS";
        drawTextWithAlpha(fpsText, currentX, startY, Fonts.getRegular(fontSize), alpha);
    }


    private float calculateTextWidth(float fontSize, float iconSize) {
        if (isShowingModState()) {
            return calculateModStateTextWidth(fontSize, iconSize);
        } else {
            return calculateNormalTextWidth(fontSize, iconSize);
        }
    }

    private float calculateModStateTextWidth(float fontSize, float iconSize) {
        if (currentModState == null) return 0;

        String modStateText = getModStateText(currentModState);
        String stateIcon = currentModState.enabled ? Icon.CHECK : Icon.CLOSE;
        return Skia.getTextBounds("Module Toggle " + stateIcon + " " + modStateText, Fonts.getRegular(fontSize)).getWidth();
    }

    private float calculateNormalTextWidth(float fontSize, float iconSize) {
        float totalWidth = 0;

        // Soar-CN图标和文本
        totalWidth += iconSize + 2;
        totalWidth += Skia.getTextBounds(name.getValue() + " · ", Fonts.getRegular(fontSize)).getWidth() + 5;

        // 玩家图标和名称
        String playerIcon = Icon.PERSON;
        totalWidth += Skia.getTextBounds(playerIcon, Fonts.getIcon(iconSize)).getWidth() + 2;
        String playerName = mc.player != null ? mc.player.getName().getString() : "NULL";
        totalWidth += Skia.getTextBounds(playerName + " · ", Fonts.getRegular(fontSize)).getWidth() + 5;

        // 服务器信息和延迟
        String linkIcon = Icon.LINK;
        totalWidth += Skia.getTextBounds(linkIcon, Fonts.getIcon(iconSize)).getWidth() + 2;
        String serverInfo = getServerInfo();
        totalWidth += Skia.getTextBounds(serverInfo + " · ", Fonts.getRegular(fontSize)).getWidth() + 5;

        // FPS图标和数值
        String fpsIcon = Icon.DESKTOP_WINDOWS;
        totalWidth += Skia.getTextBounds(fpsIcon, Fonts.getIcon(iconSize)).getWidth() + 2;
        String fpsText = mc.getCurrentFps() + " FPS";
        totalWidth += Skia.getTextBounds(fpsText, Fonts.getRegular(fontSize)).getWidth() + 5;

        return totalWidth;
    }

    private void updateAnimationState() {
        long currentTime = System.currentTimeMillis();

        // 检查是否需要切换到下一个ModState
        if (currentModState != null && currentTime - modStateStartTime > DISPLAY_DURATION) {
            if (!modStateQueue.isEmpty()) {
                // 切换到队列中的下一个
                currentModState = modStateQueue.poll();
                modStateStartTime = currentTime;
                // 重置动画状态
                isEntering = true;
                isExiting = false;
                expandProgress = 0.0f;
                textAlpha = 0.0f;
            } else {
                // 开始退出动画
                isEntering = false;
                isExiting = true;
                modStateStartTime = currentTime; // 重置计时器用于退出动画
            }
        }

        if (currentModState != null) {
            long elapsed = currentTime - modStateStartTime;

            if (isEntering) {
                // 进入动画
                float progress = Math.min(elapsed / (float) TRANSITION_DURATION, 1.0f);
                expandProgress = easeOutCubic(progress);
                textAlpha = Math.max(0, (progress - 0.3f) / 0.7f);

                if (progress >= 1.0f) {
                    isEntering = false;
                }
            } else if (isExiting) {
                // 退出动画
                float progress = Math.min(elapsed / (float) TRANSITION_DURATION, 1.0f);
                textAlpha = 1.0f - easeInCubic(Math.min(progress * 1.5f, 1.0f)); // 文字先淡出
                expandProgress = 1.0f - Math.max(0, (progress - 0.5f) / 0.5f); // 背景延迟收缩

                if (progress >= 1.0f) {
                    // 退出动画完成
                    isExiting = false;
                    currentModState = null;

                    // 检查队列中是否还有下一个
                    if (!modStateQueue.isEmpty()) {
                        currentModState = modStateQueue.poll();
                        modStateStartTime = currentTime;
                        isEntering = true;
                        expandProgress = 0.0f;
                        textAlpha = 0.0f;
                    }
                }
            } else {
                // 稳定显示阶段
                long displayTime = currentTime - (modStateStartTime + TRANSITION_DURATION);
                if (displayTime > DISPLAY_DURATION - TRANSITION_DURATION * 2) {
                    // 准备开始退出动画
                    isExiting = true;
                    modStateStartTime = currentTime - (long)(TRANSITION_DURATION * 0.7f); // 调整计时器
                }
            }
        } else {
            expandProgress = 0.0f;
            textAlpha = 0.0f;
            isEntering = false;
            isExiting = false;
        }
    }

    private void updateTextCycle() {
        if (isShowingModState()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastTextSwitchTime;

        if (showingSmtcText) {
            if (elapsed >= SMTC_TEXT_DURATION) {
                showingSmtcText = false;
                lastTextSwitchTime = currentTime;
            }
        } else {
            if (elapsed >= NORMAL_TEXT_DURATION) {
                showingSmtcText = true;
                lastTextSwitchTime = currentTime;
            }
        }
    }

    private boolean isShowingModState() {
        return currentModState != null;
    }

    // 缓动函数
    private float easeOutCubic(float x) {
        return (float) (1 - Math.pow(1 - x, 3));
    }

    private float easeInCubic(float x) {
        return x * x * x;
    }

    private float easeOutQuart(float x) {
        return (float) (1 - Math.pow(1 - x, 4));
    }

    private float easeInQuart(float x) {
        return x * x * x * x;
    }

    private @NotNull String getServerInfo() {
        if (mc.getCurrentServerEntry() != null && mc.player != null) {
            String serverAddress = mc.getCurrentServerEntry().address;
            long ping = mc.getCurrentServerEntry().ping;
            return ping + "ms to " + serverAddress;
        }
        return "Singleplayer";
    }

    private @NotNull String getModStateText(ModStateEvent modState) {
        if (modState == null) return "";

        String modName = I18n.get(modState.modName);
        String state = modState.enabled ? "enabled" : "disabled";

        return modName + " has been " + state;
    }

    private void drawTextWithAlpha(String text, float x, float y, io.github.humbleui.skija.Font font, float alpha) {
        Color color = new Color(1.0f, 1.0f, 1.0f, alpha);
        Skia.drawText(text, x, y, color, font);
    }

    private void handleModStateChange(ModStateChangeEvent event) {
        // 忽略配置加载时的事件
        if (isConfigLoading) {
            return;
        }

        // 忽略自身的事件
        if (Objects.equals(event.getMod().getName(), "Island")) {
            return;
        }

        // 创建ModStateEvent
        ModStateEvent modStateEvent = new ModStateEvent(event.getMod().getName(), event.isEnabled());

        if (currentModState == null) {
            // 如果没有正在显示的ModState，立即开始显示
            currentModState = modStateEvent;
            modStateStartTime = System.currentTimeMillis();
            isEntering = true;
            isExiting = false;
            expandProgress = 0.0f;
            textAlpha = 0.0f;
        } else {
            // 否则加入队列
            modStateQueue.offer(modStateEvent);
        }
    }

    @Override
    public float getRadius() {
        return 6;
    }

    // ModState事件内部类
    private static class ModStateEvent {
        String modName;
        boolean enabled;

        ModStateEvent(String modName, boolean enabled) {
            this.modName = modName;
            this.enabled = enabled;
        }
    }
}
