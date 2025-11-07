package com.soarclient.management.mod.impl.render;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.MouseClickEvent;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.gui.MainMenuGui;
import com.soarclient.management.mod.Mod;
import com.soarclient.management.mod.ModCategory;
import com.soarclient.management.mod.settings.impl.BooleanSetting;
import com.soarclient.management.mod.settings.impl.ColorSetting;
import com.soarclient.management.mod.settings.impl.NumberSetting;
import com.soarclient.skia.font.Icon;
import com.soarclient.utils.RippleEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.util.Window;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Shader;
import io.github.humbleui.types.Point;
import net.minecraft.data.Main;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClickEffectMod extends Mod {

    private final List<RippleEffect> ripples = new ArrayList<>();
    private int windowWidth, windowHeight;

    // 设置项
    private final ColorSetting colorSetting = new ColorSetting(
        "setting.clickeffect.color",
        "setting.clickeffect.color.description",
        Icon.TOUCHPAD_MOUSE,
        this,
        new Color(255, 255, 255, 180),
        true
    );

    private final NumberSetting maxRadiusSetting = new NumberSetting(
        "setting.clickeffect.radius",
        "setting.clickeffect.radius.description",
        Icon.CIRCLE,
        this,
        5, 5, 20, 1 // 默认10，范围5-20
    );

    private final NumberSetting durationSetting = new NumberSetting(
        "setting.clickeffect.duration",
        "setting.clickeffect.duration.description",
        Icon.TIMER,
        this,
        400, 200, 800, 50 // 默认400ms
    );

    private final BooleanSetting menuOnlySetting = new BooleanSetting(
        "setting.clickeffect.menuonly",
        "setting.clickeffect.menuonly.description",
        Icon.MONITOR,
        this,
        true
    );

    public ClickEffectMod() {
        super("mod.clickeffect.name", "mod.clickeffect.description", Icon.TOUCHPAD_MOUSE, ModCategory.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        // 注册事件监听器
        EventBus.getInstance().register(this);
        Window window = MinecraftClient.getInstance().getWindow();
        windowWidth = window.getScaledWidth();
        windowHeight = window.getScaledHeight();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        EventBus.getInstance().unregister(this);
        ripples.clear();
    }

    public final EventBus.EventListener<MouseClickEvent> onMouseClick = event -> {
        if ((mc.currentScreen instanceof TitleScreen
            || mc.currentScreen instanceof MultiplayerScreen)
            && (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
            || event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        ) {
            ripples.add(new RippleEffect(
                (float) event.getMouseX(),
                (float) event.getMouseY(),
                maxRadiusSetting.getValue(),
                (long) durationSetting.getValue()
            ));
        }
    };

    public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
        Window window = MinecraftClient.getInstance().getWindow();
        windowWidth = window.getScaledWidth();
        windowHeight = window.getScaledHeight();

        Iterator<RippleEffect> iterator = ripples.iterator();
        while (iterator.hasNext()) {
            RippleEffect ripple = iterator.next();
            if (ripple.isAlive()) {
                ripple.update();
                renderRipple(event.getCanvas(), ripple, colorSetting.getColor());
            } else {
                iterator.remove();
            }
        }
    };

    private void renderRipple(Canvas canvas, RippleEffect ripple, Color baseColor) {
        Window window = MinecraftClient.getInstance().getWindow();
        float windowHeight = window.getScaledHeight();

        float flippedY = windowHeight - ripple.getY();

        float radius = ripple.getRadius();
        float innerAlpha = ripple.getInnerAlpha();
        float outerAlpha = ripple.getOuterAlpha();

        int innerSkColor = io.github.humbleui.skija.Color.makeARGB(
            (int) (innerAlpha * baseColor.getAlpha()),
            baseColor.getRed(),
            baseColor.getGreen(),
            baseColor.getBlue()
        );

        int outerSkColor = io.github.humbleui.skija.Color.makeARGB(
            (int) (outerAlpha * baseColor.getAlpha()),
            baseColor.getRed(),
            baseColor.getGreen(),
            baseColor.getBlue()
        );

        Point center = new Point(ripple.getX(), flippedY);
        Shader shader = Shader.makeRadialGradient(
            center,
            radius,
            new int[] { innerSkColor, outerSkColor },
            new float[] { 0.0f, 1.0f }
        );

        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setAntiAlias(true);

        canvas.drawCircle(ripple.getX(), flippedY, radius, paint);
    }
}
