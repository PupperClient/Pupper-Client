package cn.pupperclient.shader;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class PupperRenderPipelines {
    private static final List<RenderPipeline> PIPELINES = new ArrayList<>();

    private static final RenderPipeline.Snippet MESH_UNIFORMS = RenderPipeline.builder()
        .withUniform("MeshData", UniformType.UNIFORM_BUFFER)
        .buildSnippet();

    public static final RenderPipeline BLUR_DOWN = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(getLocation("pipeline/blur_down"))
        .withVertexFormat(PupperVertexFormats.POS2, VertexFormat.Mode.TRIANGLES)
        .withVertexShader(Identifier.of("pupper", "blur").get())
        .withFragmentShader(Identifier.of("pupper", "blur_down").get())
        .withSampler("Sampler0")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline BLUR_UP = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(Identifier.of("pupper", "pipeline/blur_up"))
        .withVertexFormat(PupperVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(Identifier.of("pupper", "blur"))
        .withFragmentShader(Identifier.of("pupper", "blur_up"))
        .withSampler("Sampler0")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline PASSTHROUGH = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(Identifier.of("pupper", "pipeline/passthrough"))
        .withVertexFormat(PupperVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(Identifier.of("pupper", "passthrough"))
        .withFragmentShader(Identifier.of("pupper", "passthrough"))
        .withSampler("Sampler0")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline UI_COLORED = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(Identifier.of("pupper", "pipeline/ui_colored"))
        .withVertexFormat(PupperVertexFormats.POS2_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(Identifier.of("pupper", "ui_colored"))
        .withFragmentShader(Identifier.of("pupper", "ui_colored"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline UI_COLORED_LINES = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLineSmooth()
        .withLocation(Identifier.of("pupper", "pipeline/ui_colored_lines"))
        .withVertexFormat(PupperVertexFormats.POS2_COLOR, VertexFormat.DrawMode.LINES)
        .withVertexShader(Identifier.of("pupper", "ui_colored"))
        .withFragmentShader(Identifier.of("pupper", "ui_colored"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline WORLD_COLORED = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(Identifier.of("pupper", "pipeline/world_colored"))
        .withVertexFormat(PupperVertexFormats.POS3_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(Identifier.of("pupper", "pos_color.vsh")) // Uses pos_color.vsh.vsh
        .withFragmentShader(Identifier.of("pupper", "pos_color.vsh")) // Uses pos_color.vsh.fsh
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST) // 3D needs depth testing
        .withDepthWrite(false) // Usually false for ESP/Overlays to avoid glitching
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    // 3D Wireframe (Lines)
    public static final RenderPipeline WORLD_COLORED_LINES = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLineSmooth()
        .withLocation(Identifier.of("pupper", "pipeline/world_colored_lines"))
        .withVertexFormat(PupperVertexFormats.POS3_COLOR, VertexFormat.DrawMode.LINES)
        .withVertexShader(Identifier.of("pupper", "pos_color.vsh"))
        .withFragmentShader(Identifier.of("pupper", "pos_color.vsh"))
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline UI_TEXTURED = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(Identifier.of("pupper", "pipeline/ui_textured"))
        .withVertexFormat(PupperVertexFormats.POS2_COLOR_TEX, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(Identifier.of("pupper", "ui_textured"))
        .withFragmentShader(Identifier.of("pupper", "ui_textured"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withDepthWrite(false)
        .build()
    );

    public static final RenderPipeline UI_ROUNDED_TEXTURED = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(Identifier.of("pupper", "pipeline/ui_rounded_textured"))
        .withVertexFormat(PupperVertexFormats.POS2_COLOR_TEX, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(Identifier.of("pupper", "ui_textured"))
        .withFragmentShader(Identifier.of("pupper", "ui_rounded_textured")) // 指向新的 SDF Shader
        .withBlend(BlendFunction.TRANSLUCENT)
        .build()
    );

    private static RenderPipeline add(RenderPipeline pipeline) {
        PIPELINES.add(pipeline);
        return pipeline;
    }

    private PupperRenderPipelines() {}

    public static void precompile() {
        GpuDevice device = RenderSystem.getDevice();
        ResourceManager resources = Minecraft.getInstance().getResourceManager();

        for (RenderPipeline pipeline : PIPELINES) {
            device.precompilePipeline(pipeline, (identifier, shaderType) -> {
                var resource = resources.getResource(identifier).get();

                try (var in = resource.open()) {
                    return IOUtils.toString(in, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static Identifier getLocation(String path) {
        return Identifier.of("pupper", path);
    }
}
// based on meteor
