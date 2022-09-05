package caramel.api.events;

import caramel.api.objects.Scene;
import xyz.destiall.java.events.Event;

/**
 * This event is called when a scene starts playing.
 */
public final class ScenePlayEvent extends Event {
    private final Scene scene;
    public ScenePlayEvent(final Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }
}
