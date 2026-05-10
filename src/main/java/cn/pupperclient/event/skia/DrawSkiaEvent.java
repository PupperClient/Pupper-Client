/**
 * @Author: oneachina
 * @link: github.com/oneachina
 */
package cn.pupperclient.event.skia;

import cn.pupperclient.event.Event;
import io.github.humbleui.skija.Canvas;

public class DrawSkiaEvent extends Event {
    private final Canvas canvas;

    public DrawSkiaEvent(Canvas canvas) {
        this.canvas = canvas;
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
