package com.soarclient.management.mod.impl.hud;

import com.soarclient.event.EventListener;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.management.mod.api.hud.SimpleHUDMod;
import com.soarclient.management.mod.settings.impl.ComboSetting;
import com.soarclient.skia.Skia;
import com.soarclient.skia.font.Fonts;
import com.soarclient.skia.font.Icon;
import com.soarclient.utils.JNAWindowChecker;
import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.types.Rect;

import java.util.List;

public class CloudMusicHudMod extends SimpleHUDMod {
    public ComboSetting mode = new ComboSetting(
        "mod.cloudmusic.mode",
        "mod.cloudmusic.mode.description",
        Icon.MUSIC_VIDEO,
        this,
        List.of("mod.cloudmusic.jna", "mod.cloudmusic.smtc"),
        "mod.cloudmusic.jna" // 默认使用 JNA
    );

    public CloudMusicHudMod() {
        super("mod.cloudmusic.name", "mod.cloudmusic.description", Icon.MUSIC_VIDEO);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        JNAWindowChecker.startBackgroundMonitoring();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        JNAWindowChecker.stopBackgroundMonitoring();
    }

    @EventListener
    public void onSkiaRender(RenderSkiaEvent event) {
        this.draw();
    }

    @Override
    protected void draw(){
        float fontSize = 9;
        float iconSize = 10.5F;
        float padding = 5;
        boolean hasIcon = getIcon() != null && iconSetting.isEnabled();
        Rect textBounds = Skia.getTextBounds(getText(), Fonts.getRegular(fontSize));
        Rect iconBounds = Skia.getTextBounds(getIcon(), Fonts.getIcon(iconSize));
        FontMetrics metrics = Fonts.getRegular(fontSize).getMetrics();
        float width = textBounds.getWidth() + (padding * 2) + (hasIcon ? iconBounds.getWidth() + 4 : 0);
        float height = fontSize + (padding * 2) - 1.5F;
        float textCenterY = (metrics.getAscent() - metrics.getDescent()) / 2 - metrics.getAscent();

        this.begin();
        this.drawBackground(getX(), getY(), width, height);

        if (hasIcon) {
            this.drawText(getIcon(), getX() + padding, getY() + (height / 2) - (iconBounds.getHeight() / 2),
                Fonts.getIcon(iconSize));
        }

        this.drawText(getText(), getX() + padding + (hasIcon ? iconBounds.getWidth() + 4 : 0),
            getY() + (height / 2) - textCenterY, Fonts.getRegular(fontSize));
        this.finish();

        position.setSize(width, height);
    }

    @Override
    public String getText() {
        if ("mod.cloudmusic.jna".equals(mode.getOption())) {
            return JNAWindowChecker.getCurrentWindowTitle();
        } else {
            return getCloudMusicNameFromSmtc();
        }
    }

    @Override
    public String getIcon() {
        return Icon.MUSIC_NOTE;
    }

    private String getCloudMusicNameFromSmtc() {
        return "Smtc Test 没完成";
    }
}
