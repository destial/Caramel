package caramel.api.events;

import xyz.destiall.java.events.Event;

/**
 * This event is called when the window becomes focused.
 */
public final class WindowFocusEvent extends Event {
    private final boolean on;

    public WindowFocusEvent(boolean on) {
        this.on = on;
    }

    public boolean isFocused() {
        return on;
    }
}
