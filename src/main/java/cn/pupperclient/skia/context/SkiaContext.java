package cn.pupperclient.skia.context;

import java.util.function.Consumer;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import io.github.humbleui.skija.*;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33;

import com.mojang.blaze3d.systems.RenderSystem;

public class SkiaContext {

    private static DirectContext context = null;
    private static Surface surface;
    private static BackendRenderTarget renderTarget;

    public static Canvas getCanvas() {
        return surface.getCanvas();
    }

    public static void createSurface(int width, int height) {

        if (context == null) {
            context = DirectContext.makeGL();
        }

        if (surface != null) {
            surface.close();
            surface = null;
        }

        if (renderTarget != null) {
            renderTarget.close();
            renderTarget = null;
        }

        renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8,
            Framebuffer.index, GL11.GL_RGBA8);
        surface = Surface.wrapBackendRenderTarget(context, renderTarget, SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.BGRA_8888, ColorSpace.getSRGB());
    }

    public static void draw(Consumer<Canvas> drawingLogic) {

        GlStateManager._pixelStore(GlConst.GL_UNPACK_ROW_LENGTH, 0);
        GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_PIXELS, 0);
        GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_ROWS, 0);
        GlStateManager._pixelStore(GlConst.GL_UNPACK_ALIGNMENT, 4);
        GL11.glClearColor(0f, 0f, 0f, 0f);
        context.resetGLAll();

        Canvas canvas = getCanvas();
        drawingLogic.accept(canvas);

        context.flush();

        GlStateManager._glBindVertexArray(0);

        GL33.glBindSampler(0, 0);
        GlStateManager._disableBlend();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL33.glBlendEquation(GL33.GL_FUNC_ADD);
        GlStateManager._colorMask(true, true, true, true);
        GL11.glColorMask(true, true, true, true);
        GlStateManager._depthMask(true);
        GL11.glDepthMask(true);
        RenderSystem.disableScissor();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GlStateManager._disableDepthTest();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._disableCull();
    }

    public static DirectContext getContext() {
        return context;
    }
}
