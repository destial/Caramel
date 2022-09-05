package caramel.api.events;

import caramel.api.objects.Scene;
import xyz.destiall.java.events.Event;

/**
 * This event is called when a scene stops playing.
 */
public final class SceneStopEvent extends Event {
    private final Scene scene;
    public SceneStopEvent(final Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }
}
