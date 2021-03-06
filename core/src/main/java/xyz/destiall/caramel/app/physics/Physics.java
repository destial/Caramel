package xyz.destiall.caramel.app.physics;

import caramel.api.interfaces.Update;
import caramel.api.objects.GameObject;

public interface Physics extends Update {
    void addGameObject(GameObject gameObject);
    void removeGameObject(GameObject gameObject);
    void reset();
    void invalidate();

    enum Mode {
        _3D,
        _2D
    }
}
