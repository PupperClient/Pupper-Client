package cn.pupperclient.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;

public class PupperRenderer3D {
    // Pipeline references from PupperRenderPipelines
    public final PupperMeshBuilder lines = new PupperMeshBuilder(PupperRenderPipelines.WORLD_COLORED_LINES);
    public final PupperMeshBuilder triangles = new PupperMeshBuilder(PupperRenderPipelines.WORLD_COLORED);

    public void begin() {
        lines.begin();
        triangles.begin();
    }

    public void render(MatrixStack matrices) {
        // In 1.21.5, we must capture the current PositionMatrix from the stack
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Render filled sides
        if (triangles.getIndicesCount() > 0) {
            PupperMeshRenderer.begin()
                .attachments(MinecraftClient.getInstance().getFramebuffer())
                .pipeline(PupperRenderPipelines.WORLD_COLORED)
                .mesh(triangles)
                .setupCallback(pass -> {
                    pass.setUniform("u_Proj", RenderSystem.getProjectionMatrix());
                    pass.setUniform("u_ModelView", matrix);
                })
                .end();
        }

        // Render wireframe lines
        if (lines.getIndicesCount() > 0) {
            PupperMeshRenderer.begin()
                .attachments(MinecraftClient.getInstance().getFramebuffer())
                .pipeline(PupperRenderPipelines.WORLD_COLORED_LINES)
                .mesh(lines)
                .setupCallback(pass -> {
                    pass.setUniform("u_Proj", RenderSystem.getProjectionMatrix());
                    pass.setUniform("u_ModelView", matrix);
                })
                .end();
        }
    }

    // --- High Level Drawing Methods ---

    public void box(BlockPos pos, int sideColor, int lineColor, ShapeMode mode) {
        box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, sideColor, lineColor, mode);
    }

    public void box(double x1, double y1, double z1, double x2, double y2, double z2, int sideColor, int lineColor, ShapeMode mode) {
        if (mode.sides()) boxSides(x1, y1, z1, x2, y2, z2, sideColor);
        if (mode.lines()) boxLines(x1, y1, z1, x2, y2, z2, lineColor);
    }

    // --- Internal Rendering Logic ---

    public void boxSides(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        // Define 8 vertices of the cube
        int v1 = triangles.vec3(x1, y1, z1).color(color).next();
        int v2 = triangles.vec3(x1, y2, z1).color(color).next();
        int v3 = triangles.vec3(x2, y2, z1).color(color).next();
        int v4 = triangles.vec3(x2, y1, z1).color(color).next();
        int v5 = triangles.vec3(x1, y1, z2).color(color).next();
        int v6 = triangles.vec3(x1, y2, z2).color(color).next();
        int v7 = triangles.vec3(x2, y2, z2).color(color).next();
        int v8 = triangles.vec3(x2, y1, z2).color(color).next();

        // Draw 6 faces using quads (Each quad creates 2 triangles internally)
        triangles.quad(v4, v3, v2, v1); // Back
        triangles.quad(v5, v6, v7, v8); // Front
        triangles.quad(v1, v2, v6, v5); // Left
        triangles.quad(v8, v7, v3, v4); // Right
        triangles.quad(v1, v5, v8, v4); // Bottom
        triangles.quad(v2, v3, v7, v6); // Top
    }

    public void boxLines(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        int v1 = lines.vec3(x1, y1, z1).color(color).next();
        int v2 = lines.vec3(x1, y2, z1).color(color).next();
        int v3 = lines.vec3(x2, y2, z1).color(color).next();
        int v4 = lines.vec3(x2, y1, z1).color(color).next();
        int v5 = lines.vec3(x1, y1, z2).color(color).next();
        int v6 = lines.vec3(x1, y2, z2).color(color).next();
        int v7 = lines.vec3(x2, y2, z2).color(color).next();
        int v8 = lines.vec3(x2, y1, z2).color(color).next();

        // Connect vertices to form wireframe (12 lines)
        lines.line(v1, v2); lines.line(v2, v3); lines.line(v3, v4); lines.line(v4, v1); // Back face
        lines.line(v5, v6); lines.line(v6, v7); lines.line(v7, v8); lines.line(v8, v5); // Front face
        lines.line(v1, v5); lines.line(v2, v6); lines.line(v3, v7); lines.line(v4, v8); // Connecting lines
    }
}
