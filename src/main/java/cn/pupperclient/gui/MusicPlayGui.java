package cn.pupperclient.gui;

import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.skia.DrawSkiaEvent;
import cn.pupperclient.event.skia.RenderSkiaEvent;
import cn.pupperclient.gui.api.PupperGuiUtil;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;
import cn.pupperclient.utils.minecraft.interfaces.IMinecraft;
import cn.pupperclient.utils.mouse.MouseUtils;
import io.github.humbleui.types.Rect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class MusicPlayGui extends PupperGuiUtil implements IMinecraft {
    private boolean isfullscreen = false;

    public MusicPlayGui() {
        super(Component.literal("MusicPlay"));
    }

    @Override
    public void extractRenderState(final @NonNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        checkWindow();
        eventRegister();
    }

    @EventListener
    public void draw(DrawSkiaEvent event) {
        int uiWidth = 1350;
        int uiHeight = 900;

        int offsetX = (windowWidth - uiWidth) / 2;
        int offsetY = (windowHeight - uiHeight) / 2;

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
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        var mouseX = click.x();
        var mouseY = click.y();

        float uiWidth = 1350;
        float uiHeight = 900;

        float startX = (windowWidth - uiWidth) / 2;
        float startY = (windowHeight - uiHeight) / 2;

        Rect icon_CLOSE_rect =  Skia.getTextBounds(Icon.CLOSE, Fonts.getIconFill(21));
        Rect icon_FULLSCREEN_rect =  Skia.getTextBounds(Icon.FULLSCREEN, Fonts.getIconFill(21));
        Rect icon_FULLSCREEN_EXIT_rect =  Skia.getTextBounds(Icon.FULLSCREEN_EXIT, Fonts.getIconFill(21));
        Rect icon_MINIMIZE_rect =  Skia.getTextBounds(Icon.MINIMIZE, Fonts.getIconFill(21));

        if (MouseUtils.isInside(mouseX, mouseY, startX + 1320, startY + 20, icon_CLOSE_rect.getWidth(), icon_CLOSE_rect.getHeight()) ||
            MouseUtils.isInside(mouseX, mouseY, startX + 1251, startY + 25, icon_MINIMIZE_rect.getWidth(), icon_MINIMIZE_rect.getHeight())
        ) {
            client.setScreen(null);
        } else if (MouseUtils.isInside(mouseX, mouseY, startX + 1285, startY + 19, icon_FULLSCREEN_rect.getWidth() + 1, icon_FULLSCREEN_rect.getHeight() + 1)) {
            isfullscreen = !isfullscreen;
        }
        return true;
    }
}
