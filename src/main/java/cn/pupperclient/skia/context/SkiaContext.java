/*
 * Hina Client
 * Copyright (C) 2026 Hina Client
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.pupperclient.skia.context;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.EventListener;
import cn.pupperclient.event.client.RenderSkiaEvent;
import cn.pupperclient.skia.event.EventSkiaDraw;
import cn.pupperclient.skia.event.EventSkiaInit;
import cn.pupperclient.skia.gl.States;
import io.github.humbleui.skija.*;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

public class SkiaContext {
    public static final SkiaContext INSTANCE = new SkiaContext();

    private DirectContext context;
    private WrappedBackendRenderTarget renderTarget;
    private Surface surface;
    private Canvas canvas;

    public SkiaContext() {
        EventBus.getInstance().register(this);
    }

    private void initSkia(int width, int height) {
        createContext();
        createSurface(width, height);
    }

    private void createContext() {
        if (context == null) {
            context = DirectContext.makeGL();
        }
    }

    private void createSurface(int width, int height) {
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

        canvas = surface.getCanvas();
    }

    private void draw() {
        if (context == null || surface == null) return;

        States.push();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glClearColor(0f, 0f, 0f, 0f);

        context.resetGLAll();
        drawDrawables();
        context.flushAndSubmit(surface);

        States.pop();
    }

    private void drawDrawables() {
        if (canvas != null && context != null && renderTarget != null) {
            EventBus.getInstance().post(new RenderSkiaEvent(context, renderTarget, canvas));
        }
    }

    public DirectContext getContext() {
        return context;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Surface getSurface() {
        return surface;
    }

    @EventListener
    public void onInit(EventSkiaInit event) {
        initSkia(event.getWidth(), event.getHeight());
    }

    @EventListener
    public void onDraw(EventSkiaDraw event) {
        draw();
    }
}
