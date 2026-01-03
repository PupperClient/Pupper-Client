package cn.pupperclient.shader.impl;

import cn.pupperclient.shader.Framebuffer;
import cn.pupperclient.shader.MeshBuilder;
import cn.pupperclient.shader.PupperRenderPipelines;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.IntDoubleImmutablePair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;

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
        IntDoubleImmutablePair.of(5, 7.25), IntDoubleImmutablePair.of(5, 8.5) };

    private final Framebuffer[] fbos = new Framebuffer[6];
    private boolean firstTick = true;
    private MeshBuilder mesh;

    public void resize() {
        for (int i = 0; i < fbos.length; i++) {
            if (fbos[i] != null) {
                fbos[i].resize();
            } else {
                fbos[i] = new Framebuffer(1 / Math.pow(2, i));
            }
        }
    }

    public void draw(int radius) {
        if (firstTick) {
            for (int i = 0; i < fbos.length; i++) {
                if (fbos[i] == null) {
                    fbos[i] = new Framebuffer(1 / Math.pow(2, i));
                }
            }

            // 创建全屏四边形网格，使用正确的顶点数据
            mesh = new MeshBuilder(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.TRIANGLES);
            mesh.begin();

            // 顶点1: (-1, -1, 0) 纹理坐标: (0, 0)
            mesh.vec3(-1, -1, 0).vec2(0, 0).next();

            // 顶点2: (-1, 1, 0) 纹理坐标: (0, 1)
            mesh.vec3(-1, 1, 0).vec2(0, 1).next();

            // 顶点3: (1, 1, 0) 纹理坐标: (1, 1)
            mesh.vec3(1, 1, 0).vec2(1, 1).next();

            // 顶点4: (1, -1, 0) 纹理坐标: (1, 0)
            mesh.vec3(1, -1, 0).vec2(1, 0).next();

            mesh.quad(0, 1, 2, 3);
            mesh.end();

            firstTick = false;
        }

        IntDoubleImmutablePair strength = STRENGTHS[Math.min(radius - 1, STRENGTHS.length - 1)];
        int iterations = strength.leftInt();
        double offset = strength.rightDouble();

        try (GpuBuffer vertexBuffer = mesh.createVertexBuffer(); GpuBuffer indexBuffer = mesh.createIndexBuffer()) {
            // 第一遍：降采样
            renderToFbo(fbos[0],
                MinecraftClient.getInstance().getFramebuffer().getColorAttachment(),
                PupperRenderPipelines.BLUR_DOWN,
                vertexBuffer, indexBuffer, offset);

            // 多次降采样
            for (int i = 0; i < iterations; i++) {
                renderToFbo(fbos[i + 1],
                    fbos[i].getTexture(),
                    PupperRenderPipelines.BLUR_DOWN,
                    vertexBuffer, indexBuffer, offset);
            }

            // 多次上采样
            for (int i = iterations; i >= 1; i--) {
                renderToFbo(fbos[i - 1],
                    fbos[i].getTexture(),
                    PupperRenderPipelines.BLUR_UP,
                    vertexBuffer, indexBuffer, offset);
            }

            // 最终绘制到屏幕
            RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(MinecraftClient.getInstance().getFramebuffer().getColorAttachment(), OptionalInt.empty());

            pass.setPipeline(PupperRenderPipelines.PASSTHROUGH);
            pass.bindSampler("uTexture", fbos[0].getTexture());
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer, VertexFormat.IndexType.INT);
            pass.drawIndexed(0, mesh.getIndicesCount());
            pass.close();

        }
    }

    private void renderToFbo(Framebuffer target, GpuTexture source,
                             RenderPipeline pipeline,
                             GpuBuffer vertexBuffer, GpuBuffer indexBuffer,
                             double offset) {
        RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
            .createRenderPass(target.getTexture(), OptionalInt.empty());

        pass.setPipeline(pipeline);
        pass.bindSampler("uTexture", source);
        pass.setUniform("uHalfTexelSize", 0.5f / target.getTexture().getWidth(0), 0.5f / target.getTexture().getHeight(0));
        pass.setUniform("uOffset", (float) offset);
        pass.setVertexBuffer(0, vertexBuffer);
        pass.setIndexBuffer(indexBuffer, VertexFormat.IndexType.INT);
        pass.drawIndexed(0, mesh.getIndicesCount());
        pass.close();
    }

    public GpuTexture getTexture() {
        return fbos[0].getTexture();
    }
}
