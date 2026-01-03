package cn.pupperclient.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
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

    public static final VertexFormat POS2 = VertexFormat.builder()
        .add("POS2", VertexFormatElement.POSITION)
        .build();

    public static final VertexFormat POS2_TEXTURE = VertexFormat.builder()
        .add("POS2_TEXTURE", VertexFormatElement.POSITION)
        .add("UV0", VertexFormatElement.UV0)
        .build();

    // 着色器管线定义
    public static final RenderPipeline BLUR_DOWN = register(new RenderPipeline.Builder()
        .withVertexShader(read("blur.vert"))
        .withFragmentShader(read("blur_down.frag"))
        .withVertexFormat(POS2_TEXTURE, VertexFormat.DrawMode.TRIANGLES)
        .withSampler("uTexture")
        .withUniform("uHalfTexelSize", UniformType.VEC2)
        .withUniform("uOffset", UniformType.FLOAT)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build());

    public static final RenderPipeline BLUR_UP = register(new RenderPipeline.Builder()
        .withVertexShader(read("blur.vert"))
        .withFragmentShader(read("blur_up.frag"))
        .withVertexFormat(POS2_TEXTURE, VertexFormat.DrawMode.TRIANGLES)
        .withSampler("uTexture")
        .withUniform("uHalfTexelSize", UniformType.VEC2)
        .withUniform("uOffset", UniformType.FLOAT)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build());

    public static final RenderPipeline PASSTHROUGH = register(new RenderPipeline.Builder()
        .withVertexShader(read("passthrough.vert"))
        .withFragmentShader(read("passthrough.frag"))
        .withVertexFormat(POS2_TEXTURE, VertexFormat.DrawMode.TRIANGLES)
        .withSampler("uTexture")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build());

    private static RenderPipeline register(RenderPipeline pipeline) {
        PIPELINES.add(pipeline);
        return pipeline;
    }

    public class Reloader implements SynchronousResourceReloader {
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

    private static String read(String path) {
        try {
            return IOUtils.toString(
                MinecraftClient.getInstance().getResourceManager()
                    .getResource(Identifier.of("pupper", "shaders/" + path)).get().getInputStream(),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read shader '" + path + "'", e);
        }
    }
}
