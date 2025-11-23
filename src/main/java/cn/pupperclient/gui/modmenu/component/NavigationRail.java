package cn.pupperclient.gui.modmenu.component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import cn.pupperclient.PupperClient;
import cn.pupperclient.utils.mouse.ScrollHelper;
import org.lwjgl.glfw.GLFW;

import cn.pupperclient.animation.Animation;
import cn.pupperclient.animation.Duration;
import cn.pupperclient.animation.SimpleAnimation;
import cn.pupperclient.animation.cubicbezier.impl.EaseStandard;
import cn.pupperclient.animation.other.DummyAnimation;
import cn.pupperclient.gui.api.SoarGui;
import cn.pupperclient.gui.api.page.SimplePage;
import cn.pupperclient.gui.edithud.GuiEditHUD;
import cn.pupperclient.management.color.api.ColorPalette;
import cn.pupperclient.management.mod.impl.settings.ModMenuSettings;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.font.Fonts;
import cn.pupperclient.skia.font.Icon;
import cn.pupperclient.ui.component.Component;
import cn.pupperclient.ui.component.handler.impl.ButtonHandler;
import cn.pupperclient.ui.component.impl.IconButton;
import cn.pupperclient.utils.ColorUtils;
import cn.pupperclient.utils.language.I18n;
import cn.pupperclient.utils.mouse.MouseUtils;

import io.github.humbleui.skija.Font;
import io.github.humbleui.types.Rect;

public class NavigationRail extends Component {

    private final List<Navigation> navigations = new ArrayList<>();
    private Navigation currentNavigation;
    private final IconButton editButton;
    private final ScrollHelper scrollHelper = new ScrollHelper();

    private final SoarGui parent;

    public NavigationRail(SoarGui parent, float x, float y, float width, float height) {
        super(x, y);
        this.parent = parent;
        this.width = width;
        this.height = height;

        for (SimplePage p : parent.getPages()) {
            Navigation n = new Navigation(p);
            if (p.getTitle().equals(parent.getCurrentPage().getTitle())) {
                currentNavigation = n;
                n.animation = new EaseStandard(Duration.MEDIUM_3, 0, 1);
            }
            navigations.add(n);
        }

        IconButton.Size buttonSize = IconButton.Size.NORMAL;
        float buttonY = y + 44;

        editButton = new IconButton(Icon.EDIT, x, buttonY, buttonSize, IconButton.Style.TERTIARY);
        editButton.setX(x + (width / 2) - (editButton.getWidth() / 2));
        editButton.setHandler(new ButtonHandler() {
            @Override
            public void onAction() {
                parent.close(new GuiEditHUD(ModMenuSettings.getInstance().getModMenu()).build());
            }
        });
    }

    @Override
    public void draw(double mouseX, double mouseY) {
        scrollHelper.onUpdate();

        ColorPalette palette = PupperClient.getInstance().getColorManager().getPalette();

        float borderRadius = 35;
        Skia.drawRoundedRectVarying(x, y, width, height, borderRadius, 0, 0, borderRadius, palette.getSurface());

        editButton.draw(mouseX, mouseY);

        float offsetY = 140;
        float itemSpacing = 68;

        Skia.save();
        Skia.translate(0, scrollHelper.getValue());

        double translatedMouseY = mouseY - scrollHelper.getValue();

        for (Navigation n : navigations) {
            drawMd3NavItem(n, mouseX, translatedMouseY, offsetY, palette);
            offsetY += itemSpacing;
        }

        scrollHelper.setMaxScroll(offsetY - 140, height - 140);
        Skia.restore();
    }

    private void drawMd3NavItem(Navigation n, double mouseX, double mouseY, float offsetY, ColorPalette palette) {
        SimplePage p = n.page;
        String title = p.getTitle();
        String icon = p.getIcon();
        boolean isSelected = currentNavigation.equals(n);

        Font font = isSelected ? Fonts.getIconFill(24) : Fonts.getIcon(24);
        Rect bounds = Skia.getTextBounds(icon, font);
        float iconWidth = bounds.getWidth();
        float iconHeight = bounds.getHeight();

        Color c0 = isSelected ? palette.getOnSecondaryContainer() : palette.getOnSurfaceVariant();
        Color c1 = isSelected ? palette.getOnSurface() : palette.getOnSurfaceVariant();

        Animation animation = n.animation;
        float selWidth = 56;
        float selHeight = 32;
        boolean focus = MouseUtils.isInside(mouseX, mouseY, x + (width / 2) - (selWidth / 2), y + offsetY, selWidth, selHeight) || n.pressed;

        n.focusAnimation.onTick(focus ? n.pressed ? 0.12F : 0.08F : 0, 8);

        Skia.drawRoundedRect(x + (width / 2) - (selWidth / 2), y + offsetY, selWidth, selHeight, 16, ColorUtils.applyAlpha(palette.getOnSurfaceVariant(), n.focusAnimation.getValue()));

        if (animation.getEnd() != 0 || !animation.isFinished()) {
            Skia.drawRoundedRect(x + (width / 2) - (selWidth / 2) + (selWidth - selWidth * animation.getValue()) / 2, y + offsetY, selWidth * animation.getValue(), selHeight, 16, ColorUtils.applyAlpha(palette.getSecondaryContainer(), animation.getValue()));
        }

        Skia.drawText(icon, x + (width / 2) - (iconWidth / 2), y + (offsetY + (selHeight / 2)) - (iconHeight / 2), c0, font);
        Skia.drawCenteredText(I18n.get(title), x + (width / 2), y + offsetY + selHeight + 5, c1, Fonts.getMedium(12));
    }


    @Override
    public void mousePressed(double mouseX, double mouseY, int button) {
        editButton.mousePressed(mouseX, mouseY, button);
        double translatedMouseY = mouseY - scrollHelper.getValue();
        float offsetY = 140;
        float itemSpacing = 68;

        for (Navigation n : navigations) {
            float selWidth = 56;
            float selHeight = 32;
            float itemX = x + width / 2 - selWidth / 2;
            float itemY = y + offsetY;

            if (MouseUtils.isInside(mouseX, translatedMouseY, itemX, itemY, selWidth, selHeight) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !currentNavigation.equals(n)) {
                n.pressed = true;
            }
            offsetY += itemSpacing;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        editButton.mouseReleased(mouseX, mouseY, button);

        double translatedMouseY = mouseY - scrollHelper.getValue();

        float offsetY = 140;
        float itemSpacing = 68;

        for (Navigation n : navigations) {
            float selWidth = 56;
            float selHeight = 32;
            float itemX = x + (width / 2) - (selWidth / 2);
            float itemY = y + offsetY;

            if (MouseUtils.isInside(mouseX, translatedMouseY, itemX, itemY, selWidth, selHeight) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !currentNavigation.equals(n)) {
                currentNavigation.animation = new EaseStandard(Duration.MEDIUM_3, 1, 0);
                currentNavigation = n;
                parent.setCurrentPage(n.page);
                currentNavigation.animation = new EaseStandard(Duration.MEDIUM_3, 0, 1);
            }
            n.pressed = false;
            offsetY += itemSpacing;
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollHelper.onScroll(verticalAmount);
    }

    private static class Navigation {
        private final SimpleAnimation focusAnimation = new SimpleAnimation();
        private Animation animation;
        private final SimplePage page;
        private boolean pressed;

        private Navigation(SimplePage page) {
            this.page = page;
            this.animation = new DummyAnimation();
            this.pressed = false;
        }
    }
}
