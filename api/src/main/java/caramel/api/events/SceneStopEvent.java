package caramel.api.events;

import caramel.api.objects.Scene;
import xyz.destiall.java.events.Event;

public final class SceneStopEvent extends Event {
    private final Scene scene;
    public SceneStopEvent(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }
}
