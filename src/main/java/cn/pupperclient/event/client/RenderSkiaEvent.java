package cn.pupperclient.event.client;

import cn.pupperclient.event.Event;
import cn.pupperclient.skia.context.WrappedBackendRenderTarget;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.DirectContext;

public class RenderSkiaEvent extends Event {
    private final DirectContext context;
    private final WrappedBackendRenderTarget renderTarget;
    private final Canvas canvas;

    public RenderSkiaEvent(DirectContext context, WrappedBackendRenderTarget renderTarget, Canvas canvas) {
        this.context = context;
        this.renderTarget = renderTarget;
        this.canvas = canvas;
    }

    public DirectContext getContext() {
        return context;
    }

    public WrappedBackendRenderTarget getRenderTarget() {
        return renderTarget;
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
