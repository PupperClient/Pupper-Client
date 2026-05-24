/**
 * @Author: oneachina
 * @link: github.com/oneachina
 */
package cn.pupperclient.gui.api;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.utils.minecraft.interfaces.IMinecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public abstract class PupperGuiUtil extends Screen implements IMinecraft {
    public int windowWidth = 0;
    public int windowHeight = 0;

    protected boolean isEventRegister;

    protected PupperGuiUtil(final Component title) {
        super(title);
    }

    protected void checkWindow() {
        if (windowHeight == 0 || windowWidth == 0) {
            this.windowWidth = client.getWindow().getWidth();
            this.windowHeight = client.getWindow().getHeight();
        }
    }

    protected void initWindow() {
        this.windowWidth = client.getWindow().getWidth();
        this.windowHeight = client.getWindow().getHeight();
    }

    protected void eventRegister() {
        if (!isEventRegister) {
            EventBus.getInstance().register(this);
            isEventRegister = true;
        }
    }

    @Override
    public void onClose() {
        EventBus.getInstance().unregister(this);
        isEventRegister = false;
        super.onClose();
    }

    @Override
    public void removed() {
        EventBus.getInstance().unregister(this);
        isEventRegister = false;
        super.removed();
    }

    @Override
    public void init() {
        initWindow();
        eventRegister();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(final @NonNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        checkWindow();
        eventRegister();
    }
}
