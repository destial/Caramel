package caramel.api.events;

import xyz.destiall.java.events.Event;

/**
 * This event is called when the window becomes fullscreen.
 */
public final class FullscreenEvent extends Event {
    private final boolean on;

    public FullscreenEvent(boolean on) {
        this.on = on;
    }

    public boolean isOn() {
        return on;
    }
}
