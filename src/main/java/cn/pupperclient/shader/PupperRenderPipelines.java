package cn.pupperclient.shader;

import cn.pupperclient.PupperClient;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PupperRenderPipelines {
    private static final List<RenderPipeline> PIPELINES = new ArrayList<>();

    public static final RenderPipeline BLUR_DOWN = register(new RenderPipeline.Builder()
        .withLocation(PupperClient.identifier("pipeline/blur/up"))
        .withVertexFormat(PupperVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(PupperClient.identifier("shaders/passthrough.vert"))
        .withFragmentShader(PupperClient.identifier("shaders/passthrough.frag"))
        .withSampler("uTexture")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build());

    public static final RenderPipeline BLUR_UP = register(new RenderPipeline.Builder()
        .withLocation(PupperClient.identifier("pipeline/blur/up"))
        .withVertexFormat(PupperVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(PupperClient.identifier("shaders/blur.vert"))
        .withFragmentShader(PupperClient.identifier("shaders/blur_up.frag"))
        .withSampler("uTexture")
        .withUniform("uHalfTexelSize", UniformType.VEC2)
        .withUniform("uOffset", UniformType.FLOAT)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline PASSTHROUGH = register(new RenderPipeline.Builder()
        .withLocation(PupperClient.identifier("pipeline/blur/up"))
        .withVertexFormat(PupperVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(PupperClient.identifier("shaders/passthrough.vert"))
        .withFragmentShader(PupperClient.identifier("shaders/passthrough.frag"))
        .withSampler("uTexture")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    private static RenderPipeline register(RenderPipeline pipeline) {
        PIPELINES.add(pipeline);
        return pipeline;
    }

    public static class Reloader implements SynchronousResourceReloader {
        @Override
        public void reload(ResourceManager manager) {
            GpuDevice device = RenderSystem.getDevice();

            for (RenderPipeline pipeline : PIPELINES) {
                device.precompilePipeline(pipeline, (identifier, shaderType) -> {
                    var resource = manager.getResource(identifier).get();

                    try (var in = resource.getInputStream()) {
                        return IOUtils.toString(in, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}
