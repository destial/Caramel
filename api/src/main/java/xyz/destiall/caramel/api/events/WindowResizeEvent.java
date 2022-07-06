package xyz.destiall.caramel.api.events;

import xyz.destiall.java.events.Event;

public final class WindowResizeEvent extends Event {
    private final int width;
    private final int height;
    public WindowResizeEvent(int width, int height) {
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
