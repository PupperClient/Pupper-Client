package cn.pupperclient.skia.context;

import java.util.function.Consumer;

import cn.pupperclient.PupperLogger;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import io.github.humbleui.skija.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
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

        if (surface != null) surface.close();
        if (renderTarget != null) renderTarget.close();

        int activeFboId = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

        renderTarget = BackendRenderTarget.makeGL(
            width,
            height,
            0,
            8,
            activeFboId,
            GL11.GL_RGBA8
        );
        surface = Surface.wrapBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.getSRGB()
        );
    }

    public static void draw(Consumer<Canvas> drawingLogic) {
        if (context == null || surface == null) {
            PupperLogger.warn("SkiaContext", "Skip drawing: Context or Surface is null.");
            return;
        }

        GlStateManager._pixelStore(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GlStateManager._pixelStore(GL11.GL_UNPACK_ALIGNMENT, 4);

        context.resetGLAll();
        try {
            Canvas canvas = getCanvas();
            canvas.clear(0);

            drawingLogic.accept(canvas);
        } catch (Exception e) {
            PupperLogger.error("SkiaContext", "Error during Skia drawing logic", e);
        }

        context.flush();

        GlStateManager._glBindVertexArray(0);
        GlStateManager._glUseProgram(0);

        for (int i = 0; i < 12; i++) {
            GL33.glBindSampler(i, 0);
        }

        GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._enableDepthTest();
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._disableBlend();

        // RenderSystem.disableScissor();
    }

    public static DirectContext getContext() {
        return context;
    }
}
