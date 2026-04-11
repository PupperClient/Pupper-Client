package cn.pupperclient.shader;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;

public class PupperRenderer2D {
    public static PupperRenderer2D COLOR;
    public static PupperRenderer2D TEXTURE;

    private final boolean textured;

    public final PupperMeshBuilder triangles;
    public final PupperMeshBuilder lines;

    public PupperRenderer2D(boolean textured) {
        triangles = new PupperMeshBuilder(textured ? PupperRenderPipelines.UI_TEXTURED : PupperRenderPipelines.UI_COLORED);
        lines = new PupperMeshBuilder(PupperRenderPipelines.UI_COLORED_LINES);
        this.textured = textured;
    }

    static {
        COLOR = new PupperRenderer2D(false);
        TEXTURE = new PupperRenderer2D(true);
    }

    public void setAlpha(double alpha) {
        triangles.alpha = alpha;
    }

    public void begin() {
        triangles.begin();
        lines.begin();
    }

    public void end() {
        triangles.end();
        lines.end();
    }

    public void render() {
        render(null, null, null);
    }

    public void render(GpuTextureView textureView, GpuSampler sampler) {
        if (!textured)
            throw new IllegalStateException("Tried to render with a texture with a non-textured Renderer2D");

        render("u_Texture", textureView, sampler);
    }

    public void render(String samplerName, GpuTextureView samplerView, GpuSampler sampler) {
        if (lines.isBuilding()) lines.end();
        if (triangles.isBuilding()) triangles.end();

        MeshRenderer.begin()
            .attachments(Minecraft.getInstance().getMainRenderTarget())
            .pipeline(MeteorRenderPipelines.UI_COLORED_LINES)
            .mesh(lines)
            .end();

        MeshRenderer.begin()
            .attachments(Minecraft.getInstance().getMainRenderTarget())
            .pipeline(textured ? MeteorRenderPipelines.UI_TEXTURED : MeteorRenderPipelines.UI_COLORED)
            .mesh(triangles)
            .sampler(samplerName, samplerView, sampler)
            .end();
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
