package com.soarclient.event.client;

import com.soarclient.event.Event;
import io.github.humbleui.skija.Canvas;

public class RenderSkiaEvent extends Event {
    private final Canvas canvas;

    public RenderSkiaEvent(Canvas canvas) {
        this.canvas = canvas;
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
