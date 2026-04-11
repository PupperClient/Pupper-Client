package cn.pupperclient.skia.context;

import java.util.Objects;
import java.util.function.Consumer;

import cn.pupperclient.skia.api.WrappedBackendRenderTarget;
import cn.pupperclient.skia.gl.States;
import io.github.humbleui.skija.*;

import org.lwjgl.opengl.GL11;

public class SkiaContext {

    private static DirectContext context = null;
    private static Surface surface;
    private static BackendRenderTarget renderTarget;
    private static final GLBackendState[] states = {
        GLBackendState.BLEND,
        GLBackendState.VERTEX,
        GLBackendState.PIXEL_STORE,
        GLBackendState.TEXTURE_BINDING,
        GLBackendState.MISC
    };

    public static Canvas getCanvas() {
        return surface.getCanvas();
    }

    public static void createSurface(int width, int height) {
        if (context == null) {
            context = DirectContext.makeGL();
        }

        if (surface != null) surface.close();
        if (renderTarget != null) renderTarget.close();

        renderTarget = WrappedBackendRenderTarget.makeGL(
            width,
            height,
            0,
            8,
            0,
            FramebufferFormat.GR_GL_RGBA8
        );

        surface = Surface.wrapBackendRenderTarget(
            Objects.requireNonNull(context, "Context must not be null"),
            Objects.requireNonNull(renderTarget, "RenderTarget must not be null"),
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.getSRGB()
        );
    }

    public static void draw(Consumer<Canvas> drawingLogic) {
        if (context == null || surface == null) {
            return;
        }

        States.push();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glClearColor(0f, 0f, 0f, 0f);
        context.resetGL(states);

        Canvas canvas = getCanvas();
        drawingLogic.accept(canvas);

        context.flushAndSubmit(surface);
        States.pop();
    }

    public static DirectContext getContext() {
        return context;
    }
}
