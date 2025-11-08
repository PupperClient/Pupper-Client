package cn.pupperclient.management.mod.impl.hud;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.ClientTickEvent;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.management.mod.api.hud.SimpleHUDMod;
import cn.pupperclient.management.mod.settings.impl.BooleanSetting;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;

import io.github.humbleui.types.Rect;

public class CPSDisplayMod extends SimpleHUDMod {

    private final ArrayList<Long> leftPresses = new ArrayList<Long>();
    private final ArrayList<Long> rightPresses = new ArrayList<Long>();

    private final BooleanSetting rightClickSetting = new BooleanSetting("setting.rightclick",
        "setting.rightclick.description", Icon.MOUSE, this, true);

    public CPSDisplayMod() {
        super("mod.cpsdisplay.name", "mod.cpsdisplay.description", Icon.MOUSE);
    }

    public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
        this.draw();
    };

    public final EventBus.EventListener<ClientTickEvent> onClientTick = event -> {
        leftPresses.removeIf(t -> System.currentTimeMillis() - t > 1000);
        rightPresses.removeIf(t -> System.currentTimeMillis() - t > 1000);
    };

    public void onMouseClick(int button, boolean pressed) {
        if (pressed) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                leftPresses.add(System.currentTimeMillis());
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                rightPresses.add(System.currentTimeMillis());
            }
        }
    }

    @Override
    protected void draw() {
        float fontSize = 9;
        float iconSize = 10.5F;
        float padding = 5;
        boolean hasIcon = getIcon() != null && iconSetting.isEnabled();
        String text = getText();

        Rect textBounds = Skia.getTextBounds(text, Fonts.getRegular(fontSize));
        Rect iconBounds = hasIcon ? Skia.getTextBounds(getIcon(), Fonts.getIcon(iconSize)) : new Rect(0, 0, 0, 0);

        float width = textBounds.getWidth() + (padding * 2) + (hasIcon ? iconBounds.getWidth() + 4 : 0);
        float height = fontSize + (padding * 2) - 1.5F;

        this.begin();
        this.drawBackground(getX(), getY(), width, height);

        if (hasIcon) {
            Skia.drawFullCenteredText(getIcon(),
                getX() + padding + (iconBounds.getWidth() / 2),
                getY() + (height / 2),
                this.getDesign().getTextColor(),
                Fonts.getIcon(iconSize));
        }

        Skia.drawFullCenteredText(text,
            getX() + padding + (hasIcon ? iconBounds.getWidth() + 4 : 0) + (textBounds.getWidth() / 2),
            getY() + (height / 2),
            this.getDesign().getTextColor(),
            Fonts.getRegular(fontSize));

        this.finish();
        position.setSize(width, height);
    }

    @Override
    public String getText() {
        return (rightClickSetting.isEnabled() ?
            leftPresses.size() + " | " + rightPresses.size() :
            leftPresses.size()) + " CPS";
    }

    @Override
    public String getIcon() {
        return Icon.MOUSE;
    }
}
