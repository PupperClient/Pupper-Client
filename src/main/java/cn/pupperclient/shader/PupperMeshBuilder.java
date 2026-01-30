/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package cn.pupperclient.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.BufferUtils;
import java.nio.ByteBuffer;
import static org.lwjgl.system.MemoryUtil.*;

public class PupperMeshBuilder {
    public double alpha = 1;

    private final VertexFormat format;
    private final int primitiveVerticesSize;
    private final int primitiveIndicesCount;

    private ByteBuffer vertices;
    private long verticesPointerStart, verticesPointer;

    private ByteBuffer indices;
    private long indicesPointer;

    private int vertexI, indicesCount;
    private boolean building;

    public PupperMeshBuilder(RenderPipeline pipeline) {
        this(pipeline.getVertexFormat(), pipeline.getVertexFormatMode());
    }

    public PupperMeshBuilder(VertexFormat format, VertexFormat.DrawMode drawMode) {
        this.format = format;
        primitiveVerticesSize = format.getVertexSize() * drawMode.firstVertexCount;
        primitiveIndicesCount = drawMode.firstVertexCount;

        vertices = BufferUtils.createByteBuffer(primitiveVerticesSize * 256);
        verticesPointerStart = memAddress0(vertices);
        verticesPointer = verticesPointerStart;

        indices = BufferUtils.createByteBuffer(primitiveIndicesCount * 256 * 4);
        indicesPointer = memAddress0(indices);
    }

    public void begin() {
        if (building) throw new IllegalStateException("Mesh.begin() called while already building.");

        verticesPointer = verticesPointerStart;
        indicesPointer = memAddress0(indices);
        vertexI = 0;
        indicesCount = 0;
        building = true;
    }

    public PupperMeshBuilder vec2(double x, double y) {
        memPutFloat(verticesPointer, (float) x);
        memPutFloat(verticesPointer + 4, (float) y);
        verticesPointer += 8;
        return this;
    }

    public PupperMeshBuilder color(int color) {
        int r = ColorHelper.getRed(color);
        int g = ColorHelper.getGreen(color);
        int b = ColorHelper.getBlue(color);
        int a = (int) (ColorHelper.getAlpha(color) * alpha);

        memPutByte(verticesPointer, (byte) r);
        memPutByte(verticesPointer + 1, (byte) g);
        memPutByte(verticesPointer + 2, (byte) b);
        memPutByte(verticesPointer + 3, (byte) a);

        verticesPointer += 4;
        return this;
    }

    public int next() {
        return vertexI++;
    }

    public void line(int i1, int i2) {
        growIfNeeded();
        memPutInt(indicesPointer, i1);
        memPutInt(indicesPointer + 4, i2);
        indicesPointer += 8;
        indicesCount += 2;
    }

    public void quad(int i1, int i2, int i3, int i4) {
        growIfNeeded();
        memPutInt(indicesPointer, i1);
        memPutInt(indicesPointer + 4, i2);
        memPutInt(indicesPointer + 8, i3);
        memPutInt(indicesPointer + 12, i1);
        memPutInt(indicesPointer + 16, i3);
        memPutInt(indicesPointer + 20, i4);
        indicesPointer += 24;
        indicesCount += 6;
    }

    public void growIfNeeded() {
        if (getVerticesOffset() + primitiveVerticesSize >= vertices.capacity()) {
            int newSize = vertices.capacity() * 2;
            int offset = getVerticesOffset();
            ByteBuffer newVertices = BufferUtils.createByteBuffer(newSize);
            memCopy(verticesPointerStart, memAddress0(newVertices), offset);
            vertices = newVertices;
            verticesPointerStart = memAddress0(vertices);
            verticesPointer = verticesPointerStart + offset;
        }

        if ((indicesCount + primitiveIndicesCount) * 4 >= indices.capacity()) {
            int newSize = indices.capacity() * 2;
            ByteBuffer newIndices = BufferUtils.createByteBuffer(newSize);
            memCopy(memAddress0(indices), memAddress0(newIndices), indicesCount * 4L);
            indices = newIndices;
            indicesPointer = memAddress0(indices);
        }
    }

    public void end() {
        building = false;
    }

    public PupperMeshBuilder vec3(double x, double y, double z) {
        memPutFloat(verticesPointer, (float) x);
        memPutFloat(verticesPointer + 4, (float) y);
        memPutFloat(verticesPointer + 8, (float) z);
        verticesPointer += 12;
        return this;
    }

    public GpuBuffer getVertexBuffer() {
        vertices.limit(getVerticesOffset());
        return format.uploadImmediateVertexBuffer(vertices);
    }

    public GpuBuffer getIndexBuffer() {
        indices.limit(indicesCount * 4);
        return format.uploadImmediateIndexBuffer(indices);
    }

    public PupperMeshBuilder tex2(float u, float v) {
        memPutFloat(verticesPointer, u);
        memPutFloat(verticesPointer + 4, v);
        verticesPointer += 8;
        return this;
    }

    public int getIndicesCount() { return indicesCount; }
    private int getVerticesOffset() { return (int) (verticesPointer - verticesPointerStart); }
}
// based on meteor
