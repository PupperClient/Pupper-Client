package cn.pupperclient.gui.api;

import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.context.SkiaContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Base class for all PupperClient GUIs.
 * Directly extends Minecraft's Screen to ensure better compatibility and standard lifecycle.
 */
public abstract class SimpleSoarGui extends Screen {
    
    protected final Minecraft client = Minecraft.getInstance();
    protected final boolean mcScale;

    protected SimpleSoarGui(boolean mcScale) {
        super(Component.empty());
        this.mcScale = mcScale;
    }

    @Override
    protected void init() {
        // Base init logic if any
    }

    /**
     * Custom draw logic using Skia.
     */
    public abstract void draw(double mouseX, double mouseY);

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        SkiaContext.draw((skiaContext) -> {
            Skia.save();
            
            if (mcScale) {
                Skia.scale((float) client.getWindow().getGuiScale());
            }
            
            double finalMouseX = mcScale ? mouseX : client.mouseHandler.xpos();
            double finalMouseY = mcScale ? mouseY : client.mouseHandler.ypos();
            
            draw(finalMouseX, finalMouseY);
            
            Skia.restore();
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double finalMouseX = mcScale ? mouseX : client.mouseHandler.xpos();
        double finalMouseY = mcScale ? mouseY : client.mouseHandler.ypos();
        return onMousePressed(finalMouseX, finalMouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double finalMouseX = mcScale ? mouseX : client.mouseHandler.xpos();
        double finalMouseY = mcScale ? mouseY : client.mouseHandler.ypos();
        return onMouseReleased(finalMouseX, finalMouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double finalMouseX = mcScale ? mouseX : client.mouseHandler.xpos();
        double finalMouseY = mcScale ? mouseY : client.mouseHandler.ypos();
        return onMouseScrolled(finalMouseX, finalMouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return onCharTyped(chr, modifiers);
    }

    // Abstract or hook methods for subclasses to implement without overriding Screen methods directly
    
    public boolean onMousePressed(double mouseX, double mouseY, int button) { return false; }
    public boolean onMouseReleased(double mouseX, double mouseY, int button) { return false; }
    public boolean onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { return false; }
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) { return super.keyPressed(keyCode, scanCode, modifiers); }
    public boolean onCharTyped(char chr, int modifiers) { return super.charTyped(chr, modifiers); }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
