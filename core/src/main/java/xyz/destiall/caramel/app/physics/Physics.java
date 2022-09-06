package xyz.destiall.caramel.app.physics;

import caramel.api.interfaces.Update;
import caramel.api.objects.GameObject;

public interface Physics extends Update {
    void addGameObject(final GameObject gameObject);
    void removeGameObject(final GameObject gameObject);
    void reset();
    void invalidate();

    enum Mode {
        _3D,
        _2D
    }
}
