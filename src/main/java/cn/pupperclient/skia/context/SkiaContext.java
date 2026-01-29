package cn.pupperclient.skia.context;

import java.util.function.Consumer;

import io.github.humbleui.skija.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

public class SkiaContext {

    private static DirectContext context = null;
    private static Surface surface;
    private static BackendRenderTarget renderTarget;

    public static DirectContext getContext() {
        return context;
    }

    public static Canvas getCanvas() {
        return surface.getCanvas();
    }

    public static void createSurface(int width, int height) {
        if (context == null) {
            context = DirectContext.makeGL();
        }

        if (surface != null) {
            surface.close();
        }
        if (renderTarget != null) {
            renderTarget.close();
        }

        renderTarget = BackendRenderTarget.makeGL(
            width,
            height,
            0,
            8,
            0,
            0x8058
        );

        surface = Surface.wrapBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.getSRGB()
        );
    }

    public static void draw(Consumer<Canvas> drawingLogic) {
        GlStateManager._pixelStore(GlConst.GL_UNPACK_ROW_LENGTH, 0);
        GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_PIXELS, 0);
        GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_ROWS, 0);
        GlStateManager._pixelStore(GlConst.GL_UNPACK_ALIGNMENT, 4);

        context.resetGLAll();

        Canvas canvas = getCanvas();
        drawingLogic.accept(canvas);

        context.flush();

        GL33.glBindSampler(0, 0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL33.glBlendEquation(GL33.GL_FUNC_ADD);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);

        GlStateManager._glUseProgram(0);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
