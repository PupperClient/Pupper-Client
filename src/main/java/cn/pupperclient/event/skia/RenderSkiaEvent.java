package cn.pupperclient.event.skia;

import cn.pupperclient.event.Event;
import cn.pupperclient.skia.api.WrappedBackendRenderTarget;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.DirectContext;

public class RenderSkiaEvent extends Event {
    private final Canvas canvas;

    public RenderSkiaEvent(Canvas canvas) {
        this.canvas = canvas;
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
