package cn.pupperclient.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.Framebuffer;
import org.joml.Matrix4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class PupperMeshRenderer {
    private static final PupperMeshRenderer INSTANCE = new PupperMeshRenderer();

    private static boolean taken;

    private GpuTexture colorAttachment;
    private GpuTexture depthAttachment;
    private Integer clearColor;
    private RenderPipeline pipeline;
    private PupperMeshBuilder mesh;
    private Matrix4f matrix;
    Consumer<RenderPass> setupCallback;

    private PupperMeshRenderer() {}

    public static PupperMeshRenderer begin() {
        if (taken) throw new IllegalStateException("MeshRenderer already taken.");
        taken = true;

        return INSTANCE;
    }

    public PupperMeshRenderer attachments(Framebuffer framebuffer) {
        this.colorAttachment = framebuffer.getColorAttachment();
        this.depthAttachment = framebuffer.getDepthAttachment();
        return this;
    }

    public PupperMeshRenderer setupCallback(Consumer<RenderPass> setupCallback) {
        this.setupCallback = setupCallback;
        return this;
    }

    public PupperMeshRenderer pipeline(RenderPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    public PupperMeshRenderer mesh(PupperMeshBuilder mesh) {
        this.mesh = mesh;
        return this;
    }

    public void end() {
        if (pipeline != null && mesh != null && mesh.getIndicesCount() > 0) {
            GpuBuffer vertexBuffer = mesh.getVertexBuffer();
            GpuBuffer indexBuffer = mesh.getIndexBuffer();

            if (vertexBuffer != null && indexBuffer != null) {
                RenderPass pass = (depthAttachment != null && pipeline.wantsDepthTexture()) ?
                    RenderSystem.getDevice().createCommandEncoder().createRenderPass(colorAttachment, OptionalInt.of(clearColor != null ? clearColor : 0), depthAttachment, OptionalDouble.empty()) :
                    RenderSystem.getDevice().createCommandEncoder().createRenderPass(colorAttachment, OptionalInt.of(clearColor != null ? clearColor : 0));

                pass.setPipeline(pipeline);

                pass.setUniform("u_Proj", RenderSystem.getProjectionMatrix());

                if (setupCallback != null)
                    setupCallback.accept(pass);

                pass.setVertexBuffer(0, vertexBuffer);
                pass.setIndexBuffer(indexBuffer, VertexFormat.IndexType.INT);
                pass.drawIndexed(0, mesh.getIndicesCount());

                pass.close();
            }
        }

        colorAttachment = null;
        depthAttachment = null;
        clearColor = null;
        pipeline = null;
        mesh = null;
        matrix = null;
        setupCallback = null;

        taken = false;
    }
}
// based on meteor
