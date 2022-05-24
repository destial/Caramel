package xyz.destiall.caramel.app.physics;

import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.interfaces.Update;

public interface Physics extends Update {
    void addGameObject(GameObject gameObject);
    void removeGameObject(GameObject gameObject);
    void reset();

    enum Mode {
        _3D,
        _2D
    }
}
