package cn.pupperclient.gui.api;

import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.context.SkiaContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        SkiaContext.draw((_) -> {
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
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();

        double finalMouseX = mcScale ? mouseX : client.mouseHandler.xpos();
        double finalMouseY = mcScale ? mouseY : client.mouseHandler.ypos();
        return onMousePressed(finalMouseX, finalMouseY, click.button(), doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        double mouseX = click.x();
        double mouseY = click.y();

        double finalMouseX = mcScale ? mouseX : client.mouseHandler.xpos();
        double finalMouseY = mcScale ? mouseY : client.mouseHandler.ypos();
        return onMouseReleased(finalMouseX, finalMouseY, click.button());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double finalMouseX = mcScale ? mouseX : client.mouseHandler.xpos();
        double finalMouseY = mcScale ? mouseY : client.mouseHandler.ypos();
        return onMouseScrolled(finalMouseX, finalMouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return onKeyPressed(event.key(), event.scancode(), event.modifiers());
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return onCharTyped(input.codepoint());
    }

    // Abstract or hook methods for subclasses to implement without overriding Screen methods directly
    
    public boolean onMousePressed(double mouseX, double mouseY, int button, boolean doubled) { return false; }
    public boolean onMouseReleased(double mouseX, double mouseY, int button) { return false; }
    public boolean onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { return false; }
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) { return super.keyPressed(new KeyEvent(keyCode, scanCode, modifiers)); }
    public boolean onCharTyped(int chr) { return super.charTyped(new CharacterEvent(chr)); }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
