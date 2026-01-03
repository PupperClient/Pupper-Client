package cn.pupperclient.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public class Framebuffer {
    private GpuTexture texture;
    public double sizeMulti = 1;
    private FilterMode filterMode = FilterMode.LINEAR;
    private boolean mipmapEnabled = false;

    public Framebuffer(double sizeMulti) {
        this.sizeMulti = sizeMulti;
        init();
    }

    public Framebuffer() {
        init();
    }

    private void init() {
        Window window = MinecraftClient.getInstance().getWindow();

        int width = Math.max(1, (int) (window.getFramebufferWidth() * sizeMulti));
        int height = Math.max(1, (int) (window.getFramebufferHeight() * sizeMulti));

        texture = RenderSystem.getDevice().createTexture(
            () -> "PupperFramebuffer",
            TextureFormat.RGBA8,
            width,
            height,
            1
        );

        texture.setTextureFilter(filterMode, false);
        texture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
    }

    public void enableMipmap() {
        if (sizeMulti < 1.0) {
            mipmapEnabled = true;
            texture.setTextureFilter(FilterMode.LINEAR, true);
            texture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
        }
    }

    public GpuTexture getTexture() {
        return texture;
    }

    public void resize() {
        if (texture != null) {
            texture.close();
        }
        init();
        if (mipmapEnabled) {
            enableMipmap();
        }
    }

    public void close() {
        if (texture != null) {
            texture.close();
        }
    }
}
