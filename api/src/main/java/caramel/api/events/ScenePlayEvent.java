package caramel.api.events;

import caramel.api.objects.Scene;
import xyz.destiall.java.events.Event;

public final class ScenePlayEvent extends Event {
    private final Scene scene;
    public ScenePlayEvent(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }
}
