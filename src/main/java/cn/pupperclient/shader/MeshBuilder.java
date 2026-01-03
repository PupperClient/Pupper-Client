package cn.pupperclient.shader;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import static org.lwjgl.system.MemoryUtil.*;

public class MeshBuilder {
    private final VertexFormat format;
    private final VertexFormat.DrawMode drawMode;

    private final ByteBuffer vertices;
    private final long verticesPointerStart;
    private long verticesPointer;

    private final ByteBuffer indices;
    private final long indicesPointer;

    private int vertexI, indicesCount;
    private boolean building;

    public MeshBuilder(VertexFormat format, VertexFormat.DrawMode drawMode) {
        this.format = format;
        this.drawMode = drawMode;

        int vertexSize = format.getVertexSize();
        vertices = BufferUtils.createByteBuffer(vertexSize * 256 * 4);
        verticesPointerStart = memAddress0(vertices);

        indices = BufferUtils.createByteBuffer(6 * 512 * 4);
        indicesPointer = memAddress0(indices);
    }

    public void begin() {
        if (building) return;

        verticesPointer = verticesPointerStart;
        vertexI = 0;
        indicesCount = 0;
        building = true;
    }

    // 添加位置 (x, y, z)
    public MeshBuilder vec3(float x, float y, float z) {
        long p = verticesPointer;
        memPutFloat(p, x);
        memPutFloat(p + 4, y);
        memPutFloat(p + 8, z);
        verticesPointer += 12;
        return this;
    }

    // 添加纹理坐标 (u, v)
    public MeshBuilder vec2(float u, float v) {
        long p = verticesPointer;
        memPutFloat(p, u);
        memPutFloat(p + 4, v);
        verticesPointer += 8;
        return this;
    }

    public int next() {
        return vertexI++;
    }

    public void quad(int i1, int i2, int i3, int i4) {
        long p = indicesPointer + indicesCount * 4L;
        memPutInt(p, i1);
        memPutInt(p + 4, i2);
        memPutInt(p + 8, i3);
        memPutInt(p + 12, i3);
        memPutInt(p + 16, i4);
        memPutInt(p + 20, i1);
        indicesCount += 6;
    }

    public void end() {
        if (!building) return;
        building = false;
    }

    public GpuBuffer createVertexBuffer() {
        int size = (int) (verticesPointer - verticesPointerStart);
        vertices.limit(size);

        return RenderSystem.getDevice().createBuffer(
            () -> "MeshVertexBuffer",
            BufferType.VERTICES,
            BufferUsage.DYNAMIC_WRITE,
            vertices
        );
    }

    public GpuBuffer createIndexBuffer() {
        indices.limit(indicesCount * 4);
        return RenderSystem.getDevice().createBuffer(
            () -> "MeshIndexBuffer",
            BufferType.INDICES,
            BufferUsage.DYNAMIC_WRITE,
            indices
        );
    }

    public int getIndicesCount() {
        return indicesCount;
    }
}
