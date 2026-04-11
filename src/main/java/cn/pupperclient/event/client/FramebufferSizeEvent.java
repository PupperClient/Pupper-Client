/**
 * @Author: oneachina
 * @link: github.com/oneachina
 */
package cn.pupperclient.event.client;

import cn.pupperclient.event.Event;

public class FramebufferSizeEvent extends Event {
    private final int width;
    private final int height;

    public FramebufferSizeEvent(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
