package cn.pupperclient.gui.api;

import cn.pupperclient.skia.Skia;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Objects;

public class SimplePupperGui {
    public MinecraftClient client = MinecraftClient.getInstance();
    private final boolean mcScale;
    public boolean isVisible = false;

    public SimplePupperGui(boolean mcScale) {
        this.mcScale = mcScale;
    }

    public void init() {}
    public void close() {}
    public void removed() {}
    public void draw(double mouseX, double mouseY) {}
    public void mousePressed(double mouseX, double mouseY, int button) {}
    public void mouseReleased(double mouseX, double mouseY, int button) {}
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {}
    public void charTyped(char chr, int modifiers) {}
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}

    public Screen build() {
        return new Screen(Text.empty()) {
            @Override
            public void init() {
                isVisible = true;
                SimplePupperGui.this.init();
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                if (!isVisible || Objects.requireNonNull(client).currentScreen == null) return;

                Skia.save();

                if (mcScale) {
                    float scale = (float) client.getWindow().getScaleFactor();
                    Skia.scale(scale);
                }

                double scaleFactor = client.getWindow().getScaleFactor();
                double mx = mcScale ? (client.mouse.getX() / scaleFactor) : client.mouse.getX();
                double my = mcScale ? (client.mouse.getY() / scaleFactor) : client.mouse.getY();

                draw(mx, my);

                Skia.restore();
            }

            @Override
            public void close() {
                isVisible = false;
                SimplePupperGui.this.close();
                super.close();
            }

            @Override
            public void removed() {
                SimplePupperGui.this.removed();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                SimplePupperGui.this.mousePressed(getMouseX(mouseX), getMouseY(mouseY), button);
                return true;
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                SimplePupperGui.this.mouseReleased(getMouseX(mouseX), getMouseY(mouseY), button);
                return true;
            }

            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                SimplePupperGui.this.mouseScrolled(getMouseX(mouseX), getMouseY(mouseY), horizontalAmount, verticalAmount);
                return true;
            }

            private double getMouseX(double rawX) {
                if (mcScale) {
                    return rawX;
                } else {
                    assert client != null;
                    return client.mouse.getX();
                }
            }
            private double getMouseY(double rawY) {
                if (mcScale) {
                    return rawY;
                } else {
                    assert client != null;
                    return client.mouse.getY();
                }
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                SimplePupperGui.this.keyPressed(keyCode, scanCode, modifiers);
                return super.keyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            public boolean charTyped(char chr, int modifiers) {
                SimplePupperGui.this.charTyped(chr, modifiers);
                return super.charTyped(chr, modifiers);
            }

            @Override
            public boolean shouldPause() { return false; }
        };
    }
}
