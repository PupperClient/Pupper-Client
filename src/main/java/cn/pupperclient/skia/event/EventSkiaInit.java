package cn.pupperclient.skia.event;

import cn.pupperclient.event.Event;

/**
 * @Author: oneachina
 * @Date: 2026/2/3 11:36
 */
public class EventSkiaInit extends Event {
    private final int width;
    private final int height;

    public EventSkiaInit(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
