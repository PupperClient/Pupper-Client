package cn.pupperclient.gui;

import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.skia.DrawSkiaEvent;
import cn.pupperclient.gui.api.PupperGuiUtil;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;
import cn.pupperclient.utils.minecraft.interfaces.IMinecraft;
import cn.pupperclient.utils.mouse.MouseUtils;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class MusicPlayGui extends PupperGuiUtil implements IMinecraft {
    private boolean isfullscreen = false;

    private final int uiWidth = 1350;
    private final int uiHeight = 900;

    public MusicPlayGui() {
        super(Component.literal("MusicPlay"));
    }

    @EventListener
    public void draw(DrawSkiaEvent event) {
        var offsetX = (windowWidth - uiWidth) / 2;
        var offsetY = (windowHeight - uiHeight) / 2;

        Skia.translate(offsetX, offsetY);
        Skia.drawRect(0, 0, 340, uiHeight, new Color(20, 20, 20, 240));
        Skia.drawRect(339, 0, 1015, uiHeight, new Color(10, 10, 10, 240));

        Skia.drawText("Minecraft Jagget MusicPlay", 20, 26, Color.WHITE, Fonts.getRegular(23));

        Skia.drawText(Icon.CLOSE, 1320, 20, Color.WHITE, Fonts.getIconFill(21));

        if (!isfullscreen) {
            Skia.drawText(Icon.FULLSCREEN, 1285, 19, Color.WHITE, Fonts.getIconFill(21));
        } else {
            Skia.drawText(Icon.FULLSCREEN_EXIT, 1285, 19, Color.WHITE, Fonts.getIconFill(21));
        }
        Skia.drawText(Icon.MINIMIZE, 1251, 25, Color.WHITE, Fonts.getIconFill(21));
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        var mouseX = click.x();
        var mouseY = click.y();

        var offsetX = (windowWidth - uiWidth) / 2;
        var offsetY = (windowHeight - uiHeight) / 2;

        var icon_CLOSE_rect = Skia.getTextBounds(Icon.CLOSE, Fonts.getIconFill(21));
        var icon_FULLSCREEN_rect = Skia.getTextBounds(Icon.FULLSCREEN, Fonts.getIconFill(21));
        var icon_MINIMIZE_rect = Skia.getTextBounds(Icon.MINIMIZE, Fonts.getIconFill(21));

        if (MouseUtils.isInside(mouseX, mouseY, offsetX + 1320, offsetY + 20, icon_CLOSE_rect.getWidth(), icon_CLOSE_rect.getHeight()) ||
            MouseUtils.isInside(mouseX, mouseY, offsetX + 1251, offsetY + 25, icon_MINIMIZE_rect.getWidth(), icon_MINIMIZE_rect.getHeight())
        ) {
            client.setScreen(null);
            return true;
        }
        if (MouseUtils.isInside(mouseX, mouseY, offsetX + 1285, offsetY + 19, icon_FULLSCREEN_rect.getWidth() + 1, icon_FULLSCREEN_rect.getHeight() + 1)) {
            isfullscreen = !isfullscreen;
            return true;
        }
        return false;
    }
}
