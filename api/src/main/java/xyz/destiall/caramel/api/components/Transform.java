package xyz.destiall.caramel.api.components;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.interfaces.HideInEditor;

/**
 * This {@link Component} represents the transform matrices of the {@link GameObject}.
 */
public final class Transform extends Component {
    public final Vector3f position;
    public final Vector3f localPosition;
    public final Vector3f rotation;
    public final Vector3f scale;

    @HideInEditor public transient final Matrix4f model;
    @HideInEditor public final Vector3f forward;
    @HideInEditor public final Vector3f up;

    public Transform(GameObject gameObject) {
        super(gameObject);
        gameObject.transform = this;
        position = new Vector3f(0f, 0f, -1f);
        localPosition = new Vector3f(0f, 0f, 0f);
        rotation = new Vector3f(0f, 0f, 0f);
        scale = new Vector3f(1, 1, 1);
        forward = new Vector3f(0, 0, -1);
        up = new Vector3f(0, 1, 0);
        model = new Matrix4f().identity();
        gameObject.addComponent(this);
    }

    @Override
    public void start() {}

    public void update() {}
}
