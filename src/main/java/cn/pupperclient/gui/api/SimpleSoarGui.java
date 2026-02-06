package cn.pupperclient.gui.api;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.skia.Skia;
import cn.pupperclient.skia.event.EventSkiaDraw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SimpleSoarGui {
    public MinecraftClient client = MinecraftClient.getInstance();
    private final boolean mcScale;
    private double currentMouseX;
    private double currentMouseY;

    public SimpleSoarGui(boolean mcScale) {
        this.mcScale = mcScale;
    }

    public void init() {
    }

    public void draw(double mouseX, double mouseY) {
    }

    public void mousePressed(double mouseX, double mouseY, int button) {
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    }

    public void charTyped(char chr, int modifiers) {
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    public Screen build() {
        return new Screen(Text.empty()) {
            @Override
            public void init() {
                SimpleSoarGui.this.init();
                EventBus.getInstance().register(this);
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                if (mcScale) {
                    currentMouseX = mouseX;
                } else {
                    assert client != null;
                    currentMouseX = client.mouse.getX();
                }
                currentMouseY = mcScale ? mouseY : client.mouse.getY();
            }

            @Override
            public void close() {
                super.close();
                EventBus.getInstance().unregister(this);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (client != null) {
                    SimpleSoarGui.this.mousePressed(mcScale ? mouseX : client.mouse.getX(),
                        mcScale ? mouseY : client.mouse.getY(), button);
                }
                return true;
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (client != null) {
                    SimpleSoarGui.this.mouseReleased(mcScale ? mouseX : client.mouse.getX(),
                        mcScale ? mouseY : client.mouse.getY(), button);
                }
                return true;
            }

            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                if (client != null) {
                    SimpleSoarGui.this.mouseScrolled(mcScale ? mouseX : client.mouse.getX(),
                        mcScale ? mouseY : client.mouse.getY(), horizontalAmount, verticalAmount);
                }
                return true;
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                SimpleSoarGui.this.keyPressed(keyCode, scanCode, modifiers);
                return true;
            }

            @Override
            public boolean charTyped(char chr, int modifiers) {
                SimpleSoarGui.this.charTyped(chr, modifiers);
                return true;
            }

            @Override
            public boolean shouldPause() {
                return false;
            }
        };
    }

    @EventListener
    public void onRenderSkia(RenderSkiaEvent event) {
        Skia.save();
        if (mcScale && client != null) {
            Skia.scale((float) client.getWindow().getScaleFactor());
        }
        this.draw(currentMouseX, currentMouseY);
        Skia.restore();
    }
}
