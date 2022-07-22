package xyz.destiall.caramel.app.physics;

import caramel.api.objects.GameObject;
import caramel.api.interfaces.Update;

public interface Physics extends Update {
    void addGameObject(GameObject gameObject);
    void removeGameObject(GameObject gameObject);
    void reset();

    enum Mode {
        _3D,
        _2D
    }
}
