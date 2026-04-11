package cn.pupperclient.shader.impl;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.client.FramebufferSizeEvent;
import cn.pupperclient.shader.*;
import cn.pupperclient.shader.patch.FixedUniformStorage;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.IntFloatImmutablePair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniformStorage;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class Kawaseblur {
    public static final Kawaseblur GUI_BLUR = new Kawaseblur();
    public static final Kawaseblur INGAME_BLUR = new Kawaseblur();
    private final Minecraft mc = Minecraft.getInstance();

    private final IntFloatImmutablePair[] strengths = new IntFloatImmutablePair[]{
        IntFloatImmutablePair.of(1, 1.25f), // LVL 1
        IntFloatImmutablePair.of(1, 2.25f), // LVL 2
        IntFloatImmutablePair.of(2, 2.0f),  // LVL 3
        IntFloatImmutablePair.of(2, 3.0f),  // LVL 4
        IntFloatImmutablePair.of(2, 4.25f), // LVL 5
        IntFloatImmutablePair.of(3, 2.5f),  // LVL 6
        IntFloatImmutablePair.of(3, 3.25f), // LVL 7
        IntFloatImmutablePair.of(3, 4.25f), // LVL 8
        IntFloatImmutablePair.of(3, 5.5f),  // LVL 9
        IntFloatImmutablePair.of(4, 3.25f), // LVL 10
        IntFloatImmutablePair.of(4, 4.0f),  // LVL 11
        IntFloatImmutablePair.of(4, 5.0f),  // LVL 12
        IntFloatImmutablePair.of(4, 6.0f),  // LVL 13
        IntFloatImmutablePair.of(4, 7.25f), // LVL 14
        IntFloatImmutablePair.of(4, 8.25f), // LVL 15
        IntFloatImmutablePair.of(5, 4.5f),  // LVL 16
        IntFloatImmutablePair.of(5, 5.25f), // LVL 17
        IntFloatImmutablePair.of(5, 6.25f), // LVL 18
        IntFloatImmutablePair.of(5, 7.25f), // LVL 19
        IntFloatImmutablePair.of(5, 8.5f)   // LVL 20
    };

    private final GpuTextureView[] fbos = new GpuTextureView[6];
    private GpuBufferSlice[] ubos;

    private boolean enabled;
    private long fadeEndAt;
    private float previousOffset = -1;

    private Kawaseblur() {
        EventBus.getInstance().register(this);

        for (int i = 0; i < fbos.length; i++) {
            fbos[i] = createFbo(i);
        }
    }

    public void draw(CommandEncoder encoder, int iterations) {
        if (iterations <= 0) return;
        GpuTextureView mainFbo = mc.getFramebuffer();

        renderPass(encoder, fbos[0], mainFbo.getColorAttachment(), PupperRenderPipelines.PASSTHROUGH, 0);

        for (int i = 0; i < iterations; i++) {
            renderPass(encoder, fbos[Math.min(i + 1, 4)], fbos[i].getColorAttachment(), PupperRenderPipelines.BLUR_DOWN, i);
        }

        for (int i = iterations; i > 0; i--) {
            renderPass(encoder, fbos[i - 1], fbos[i].getColorAttachment(), PupperRenderPipelines.BLUR_UP, i);
        }

        renderPass(encoder, mainFbo, fbos[0].getColorAttachment(), PupperRenderPipelines.PASSTHROUGH, 0);
    }

    private void renderToFbo(GpuTextureView targetFbo, GpuTextureView sourceTexture, RenderPipeline pipeline, GpuBufferSlice ubo) {
        PupperMeshRenderer.begin()
            .attachments(targetFbo, null)
            .pipeline(pipeline)
            .fullscreen()
            .uniform("BlurData", ubo)
            .sampler("u_Texture", sourceTexture, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR))
            .end();
    }

    public GpuTextureView[] getFbos() {
        return fbos;
    }

    private GpuTextureView createFbo(int i, int baseWidth, int baseHeight) {
        double scale = 1 / Math.pow(2, i);
        int width = (int) (baseWidth * scale);
        int height = (int) (baseHeight * scale);

        width = Math.max(width, 1);
        height = Math.max(height, 1);

        return RenderSystem.getDevice().createTextureView(RenderSystem.getDevice().createTexture("Blur - " + i, 15,  TextureFormat.RGBA8, width, height, 1, 1));
    }

    private void rebuildFbos(int w, int h) {
        for (int i = 0; i < fbos.length; i++) {
            if (fbos[i] != null) {
                fbos[i].close();
            }
            fbos[i] = createFbo(i, w, h);
        }
        previousOffset = -1;
    }

    @EventListener
    private void onFramebufferSize(FramebufferSizeEvent event) {
        int newWidth = event.getWidth();
        int newHeight = event.getHeight();

        rebuildFbos(newWidth, newHeight);
    }

    // Uniforms
    private void updateUniforms(float offset) {
        UNIFORM_STORAGE.clear();

        BlurUniformData[] uboData = new BlurUniformData[6];
        for (int i = 0; i < uboData.length; i++) {
            GpuTextureView fbo = fbos[i];
            uboData[i] = new BlurUniformData(
                0.5f / fbo.getWidth(0), 0.5f / fbo.getHeight(0),
                offset
            );
        }

        ubos = UNIFORM_STORAGE.writeAll(uboData);
    }

    private static final int UNIFORM_SIZE = new Std140SizeCalculator()
        .putVec2()
        .putFloat()
        .get();

    private static final FixedUniformStorage<BlurUniformData> UNIFORM_STORAGE = new FixedUniformStorage<>("Meteor - Blur UBO", UNIFORM_SIZE, 6);

    private record BlurUniformData(float halfTexelSizeX, float halfTexelSizeY, float offset) implements DynamicUniformStorage.DynamicUniform {
        @Override
        public void write(@NonNull ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                .putVec2(halfTexelSizeX, halfTexelSizeY)
                .putFloat(offset);
        }
    }
}
