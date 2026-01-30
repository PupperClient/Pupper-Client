package cn.pupperclient.shader.impl;

import cn.pupperclient.shader.*;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;

public class Kawaseblur {
    public static final Kawaseblur GUI_BLUR = new Kawaseblur();
    public static final Kawaseblur INGAME_BLUR = new Kawaseblur();

    private Framebuffer[] fbos;
    private int lastWidth, lastHeight;

    private Kawaseblur() {}

    /**
     * 当窗口大小改变时调用，重新初始化 FBO
     */
    public void resize() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int width = mc.getWindow().getFramebufferWidth();
        int height = mc.getWindow().getFramebufferHeight();

        if (width <= 0 || height <= 0 || (width == lastWidth && height == lastHeight)) return;

        if (fbos != null) {
            for (Framebuffer fb : fbos) if (fb != null) fb.delete();
        }

        fbos = new Framebuffer[5];
        for (int i = 0; i < 5; i++) {
            fbos[i] = new SimpleFramebuffer("kawase_blur_" + i, width, height, false);
        }

        lastWidth = width;
        lastHeight = height;
    }

    /**
     * 执行模糊渲染逻辑
     * @param encoder 1.21.5 的命令编码器
     * @param iterations 模糊强度/迭代次数
     */
    public void draw(CommandEncoder encoder, int iterations) {
        if (fbos == null || iterations <= 0) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer mainFbo = mc.getFramebuffer();

        // 1. Initial Pass: 把主 FBO 拷贝到第一个模糊 FBO (使用 Passthrough)
        renderPass(encoder, fbos[0], mainFbo.getColorAttachment(), PupperRenderPipelines.PASSTHROUGH, 0);

        // 2. Downsample Passes: 逐层下采样并模糊
        for (int i = 0; i < iterations; i++) {
            renderPass(encoder, fbos[Math.min(i + 1, 4)], fbos[i].getColorAttachment(), PupperRenderPipelines.BLUR_DOWN, i);
        }

        // 3. Upsample Passes: 逐层恢复尺寸
        for (int i = iterations; i > 0; i--) {
            renderPass(encoder, fbos[i - 1], fbos[i].getColorAttachment(), PupperRenderPipelines.BLUR_UP, i);
        }

        // 4. Final Pass: 将结果画回主 FBO (或者你可以在这里直接渲染到屏幕)
        renderPass(encoder, mainFbo, fbos[0].getColorAttachment(), PupperRenderPipelines.PASSTHROUGH, 0);
    }

    private void renderPass(CommandEncoder encoder, Framebuffer targetFbo, GpuTexture sourceTex, RenderPipeline pipeline, float offset) {
        if (targetFbo == null || sourceTex == null) return;

        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);

        PupperMeshRenderer.begin()
            .attachments(targetFbo)
            .pipeline(pipeline)
            .mesh(PupperFullScreenRenderer.mesh)
            .setupCallback(pass -> {
                pass.setUniform("u_Proj", RenderSystem.getProjectionMatrix());
                pass.setUniform("u_ModelView", RenderSystem.getModelViewStack());

                pass.setUniform("uOffset", offset);
                pass.setUniform("uHalfTexelSize", 0.5f / (float)targetFbo.textureWidth, 0.5f / (float)targetFbo.textureHeight);

                pass.bindSampler("Sampler0", sourceTex);
            })
            .end();

        GlStateManager._enableDepthTest();
        GlStateManager._depthMask(true);
    }

    public Framebuffer[] getFbos() {
        return fbos;
    }
}
