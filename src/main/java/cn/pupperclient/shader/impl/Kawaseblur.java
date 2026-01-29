package cn.pupperclient.shader.impl;

import cn.pupperclient.management.mod.impl.settings.SystemSettings;
import cn.pupperclient.shader.*;
import cn.pupperclient.utils.TimerUtils;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.IntDoubleImmutablePair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.texture.GlTexture;
import org.joml.Matrix4f;

import java.util.OptionalInt;

public class Kawaseblur {

    public static final Kawaseblur GUI_BLUR = new Kawaseblur();
    public static final Kawaseblur INGAME_BLUR = new Kawaseblur();

    private static final IntDoubleImmutablePair[] STRENGTHS = new IntDoubleImmutablePair[] {
        IntDoubleImmutablePair.of(1, 1.25), IntDoubleImmutablePair.of(1, 2.25), IntDoubleImmutablePair.of(2, 2.0),
        IntDoubleImmutablePair.of(2, 3.0), IntDoubleImmutablePair.of(2, 4.25), IntDoubleImmutablePair.of(3, 2.5),
        IntDoubleImmutablePair.of(3, 3.25), IntDoubleImmutablePair.of(3, 4.25), IntDoubleImmutablePair.of(3, 5.5),
        IntDoubleImmutablePair.of(4, 3.25), IntDoubleImmutablePair.of(4, 4.0), IntDoubleImmutablePair.of(4, 5.0),
        IntDoubleImmutablePair.of(4, 6.0), IntDoubleImmutablePair.of(4, 7.25), IntDoubleImmutablePair.of(4, 8.25),
        IntDoubleImmutablePair.of(5, 4.5), IntDoubleImmutablePair.of(5, 5.25), IntDoubleImmutablePair.of(5, 6.25),
        IntDoubleImmutablePair.of(5, 7.25), IntDoubleImmutablePair.of(5, 8.5)
    };

    private final Framebuffer[] fbos = new Framebuffer[6];
    private final TimerUtils timer = new TimerUtils();
    private boolean initialized = false;

    public void resize() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int width = mc.getWindow().getFramebufferWidth();
        int height = mc.getWindow().getFramebufferHeight();

        for (int i = 0; i < fbos.length; i++) {
            if (fbos[i] != null) {
                fbos[i].delete();
            }
            double factor = 1.0 / Math.pow(2, i);
            int fboW = Math.max(1, (int) (width * factor));
            int fboH = Math.max(1, (int) (height * factor));
            fbos[i] = new SimpleFramebuffer("KawaseBlur_FBO_" + i, fboW, fboH, false);
        }
        initialized = true;
    }

    public void draw(int radius) {
        if (!initialized) resize();

        SystemSettings setting = SystemSettings.getInstance();
        if (setting.isFastBlur() && !timer.delay(16)) return;
        timer.reset();

        IntDoubleImmutablePair strength = STRENGTHS[Math.min(radius - 1, STRENGTHS.length - 1)];
        int iterations = strength.leftInt();
        float offset = (float) strength.rightDouble();

        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer mainFbo = mc.getFramebuffer();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

        renderPass(encoder, fbos[0], mainFbo.getColorAttachment(), PupperRenderPipelines.BLUR_DOWN, offset);
        for (int i = 0; i < iterations; i++) {
            renderPass(encoder, fbos[i + 1], fbos[i].getColorAttachment(), PupperRenderPipelines.BLUR_DOWN, offset);
        }

        for (int i = iterations; i >= 1; i--) {
            renderPass(encoder, fbos[i - 1], fbos[i].getColorAttachment(), PupperRenderPipelines.BLUR_UP, offset);
        }

        renderPass(encoder, mainFbo, fbos[0].getColorAttachment(), PupperRenderPipelines.PASSTHROUGH, offset);

        ShaderHelper.fullReset();
    }

    private void renderPass(CommandEncoder encoder, Framebuffer targetFbo, GpuTexture sourceTex, RenderPipeline pipeline, float offset) {
        if (targetFbo.getColorAttachment() == null || sourceTex == null) return;

        PostProcessRenderer.init();

        try (RenderPass pass = encoder.createRenderPass(targetFbo.getColorAttachment(), OptionalInt.empty())) {
            pass.setPipeline(pipeline);
            pass.setUniform("u_Proj", RenderSystem.getProjectionMatrix());
            pass.setUniform("u_ModelView", new Matrix4f(RenderSystem.getModelViewStack()));
            pass.setUniform("uOffset", offset);
            pass.setUniform("uHalfTexelSize", 0.5f / (float)targetFbo.textureWidth, 0.5f / (float)targetFbo.textureHeight);

            pass.bindSampler("Sampler0", sourceTex);

            pass.setVertexBuffer(0, PostProcessRenderer.getVertices());
            pass.setIndexBuffer(PostProcessRenderer.getIndices(), VertexFormat.IndexType.INT);

            pass.drawIndexed(0, 6);
        }
    }

    public int getTexture() {
        if (fbos[0] != null) {
            GpuTexture texture = fbos[0].getColorAttachment();
            if (texture instanceof GlTexture glTexture) {
                return glTexture.getGlId();
            }
        }
        return -1;
    }
}
