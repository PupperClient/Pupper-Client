package cn.pupperclient.gui.api;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.skia.RenderSkiaEvent;
import cn.pupperclient.skia.Skia;
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
    private boolean registered;

    protected SimpleSoarGui(boolean mcScale) {
        super(Component.empty());
        this.mcScale = true;
    }

    @Override
    protected void init() {
        if (client.level != null && !registered) {
            EventBus.getInstance().register(this);
            registered = true;
        }
    }

    /**
     * Custom draw logic using Skia.
     */
    public abstract void draw(double mouseX, double mouseY);

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (client.level != null && !registered) {
            EventBus.getInstance().register(this);
            registered = true;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double guiScale = client.getWindow().getGuiScale();
        return onMousePressed(client.mouseHandler.xpos() / guiScale, client.mouseHandler.ypos() / guiScale, click.button(), doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        double guiScale = client.getWindow().getGuiScale();
        return onMouseReleased(client.mouseHandler.xpos() / guiScale, client.mouseHandler.ypos() / guiScale, click.button());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double guiScale = client.getWindow().getGuiScale();
        return onMouseScrolled(client.mouseHandler.xpos() / guiScale, client.mouseHandler.ypos() / guiScale, horizontalAmount, verticalAmount);
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
    public void removed() {
        if (registered) {
            EventBus.getInstance().unregister(this);
            registered = false;
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @EventListener
    public void onRenderSkia(RenderSkiaEvent event) {
        if (client.level == null) {
            return;
        }
        if (client.screen == this) {
            double guiScale = client.getWindow().getGuiScale();
            Skia.save();
            draw(client.mouseHandler.xpos() / guiScale, client.mouseHandler.ypos() / guiScale);
            Skia.restore();
        }
    }
}
