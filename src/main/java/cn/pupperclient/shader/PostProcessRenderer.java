package cn.pupperclient.shader;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.BufferUtils;
import java.nio.ByteBuffer;

public class PostProcessRenderer {

    private static GpuBuffer vertexBuffer;
    private static GpuBuffer indexBuffer;

    public static void init() {
        if (vertexBuffer != null) return;

        ByteBuffer vData = BufferUtils.createByteBuffer(4 * 2 * 4);
        vData.putFloat(-1f).putFloat(-1f);
        vData.putFloat(-1f).putFloat(1f);
        vData.putFloat(1f).putFloat(1f);
        vData.putFloat(1f).putFloat(-1f);
        vData.flip();

        ByteBuffer iData = BufferUtils.createByteBuffer(6 * 4);
        iData.putInt(0).putInt(1).putInt(2);
        iData.putInt(2).putInt(3).putInt(0);
        iData.flip();

        var device = RenderSystem.getDevice();
        vertexBuffer = device.createBuffer(() -> "PostProcessVBO", BufferType.VERTICES, BufferUsage.STATIC_WRITE, vData);
        indexBuffer = device.createBuffer(() -> "PostProcessIBO", BufferType.INDICES, BufferUsage.STATIC_WRITE, iData);
    }

    public static GpuBuffer getVertices() {
        return vertexBuffer;
    }

    public static GpuBuffer getIndices() {
        return indexBuffer;
    }

    public static void beginRender() {}
    public static void render() {}
    public static void endRender() {}
}
