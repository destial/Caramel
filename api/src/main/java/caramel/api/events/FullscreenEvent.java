package caramel.api.events;

import xyz.destiall.java.events.Event;

public final class FullscreenEvent extends Event {
    private final boolean on;

    public FullscreenEvent(boolean on) {
        this.on = on;
    }

    public boolean isOn() {
        return on;
    }
}
