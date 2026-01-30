package cn.pupperclient.shader;

import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.MinecraftClient;
import java.util.function.Consumer;

public class PupperRenderer2D {
    public static PupperRenderer2D COLOR;

    public final PupperMeshBuilder triangles;
    public final PupperMeshBuilder lines;
    public final PupperMeshBuilder texturedMesh;

    public PupperRenderer2D() {
        triangles = new PupperMeshBuilder(PupperRenderPipelines.UI_COLORED);
        lines = new PupperMeshBuilder(PupperRenderPipelines.UI_COLORED_LINES);
        texturedMesh = new PupperMeshBuilder(PupperRenderPipelines.UI_TEXTURED);
    }

    public static void init() {
        COLOR = new PupperRenderer2D();
    }

    public void setAlpha(double alpha) {
        triangles.alpha = alpha;
        lines.alpha = alpha;
    }

    public void begin() {
        triangles.begin();
        lines.begin();
    }

    public void end() {
        triangles.end();
        lines.end();

        render(null);
    }

    public void render(Consumer<RenderPass> setupCallback) {
        if (triangles.getIndicesCount() > 0) {
            PupperMeshRenderer.begin()
                .attachments(MinecraftClient.getInstance().getFramebuffer())
                .pipeline(PupperRenderPipelines.UI_COLORED)
                .mesh(triangles)
                .setupCallback(setupCallback)
                .end();
        }

        if (lines.getIndicesCount() > 0) {
            PupperMeshRenderer.begin()
                .attachments(MinecraftClient.getInstance().getFramebuffer())
                .pipeline(PupperRenderPipelines.UI_COLORED_LINES)
                .mesh(lines)
                .setupCallback(setupCallback)
                .end();
        }
    }

    public void quad(double x, double y, double width, double height, int color) {
        int i1 = triangles.vec2(x, y).color(color).next();
        int i2 = triangles.vec2(x, y + height).color(color).next();
        int i3 = triangles.vec2(x + width, y + height).color(color).next();
        int i4 = triangles.vec2(x + width, y).color(color).next();

        triangles.quad(i1, i2, i3, i4);
    }

    public void line(double x1, double y1, double x2, double y2, int color) {
        lines.line(
            lines.vec2(x1, y1).color(color).next(),
            lines.vec2(x2, y2).color(color).next()
        );
    }

    public PupperMeshBuilder getTexturedMesh() {
        return texturedMesh;
    }
}
// based on meteor
