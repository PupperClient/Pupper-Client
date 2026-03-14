package cn.pupperclient.gui.api;

import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.context.SkiaContext;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Base class for all PupperClient GUIs.
 * Directly extends Minecraft's Screen to ensure better compatibility and standard lifecycle.
 */
public abstract class SimpleSoarGui extends Screen {
    
    protected final MinecraftClient client = MinecraftClient.getInstance();
    protected final boolean mcScale;

    protected SimpleSoarGui(boolean mcScale) {
        super(Text.empty());
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        SkiaContext.draw((skiaContext) -> {
            Skia.save();
            
            if (mcScale) {
                Skia.scale((float) client.getWindow().getScaleFactor());
            }

            // Standardize mouse coordinates based on scaling
            double finalMouseX = mcScale ? mouseX : client.mouse.getX();
            double finalMouseY = mcScale ? mouseY : client.mouse.getY();
            
            draw(finalMouseX, finalMouseY);
            
            Skia.restore();
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double finalMouseX = mcScale ? mouseX : client.mouse.getX();
        double finalMouseY = mcScale ? mouseY : client.mouse.getY();
        return onMousePressed(finalMouseX, finalMouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double finalMouseX = mcScale ? mouseX : client.mouse.getX();
        double finalMouseY = mcScale ? mouseY : client.mouse.getY();
        return onMouseReleased(finalMouseX, finalMouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double finalMouseX = mcScale ? mouseX : client.mouse.getX();
        double finalMouseY = mcScale ? mouseY : client.mouse.getY();
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
    public boolean shouldPause() {
        return false;
    }
}
