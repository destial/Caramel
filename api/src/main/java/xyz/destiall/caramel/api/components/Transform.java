package xyz.destiall.caramel.api.components;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.interfaces.HideInEditor;

public final class Transform extends Component {
    public final Vector3f position;
    public final Vector3f rotation;
    public final Vector3f scale;

    @HideInEditor public transient final Matrix4f model;
    @HideInEditor public final Vector3f forward;
    @HideInEditor public final Vector3f up;

    public Transform(GameObject gameObject) {
        super(gameObject);
        gameObject.transform = this;
        transform = this;
        position = new Vector3f(0f, 0f, -1f);
        rotation = new Vector3f(0f, 0f, 0f);
        scale = new Vector3f(1f, 1f, 1f);
        forward = new Vector3f(0f, 0f, -1f);
        up = new Vector3f(0f, 1f, 0f);
        model = new Matrix4f();
        gameObject.addComponent(this);
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        RigidBody rb = getComponent(RigidBody.class);
        if (rb != null) {
            rb._setPosition(x, y, z);
        }
    }

    @Override
    public void start() {}

    public void update() {}

    @Override
    public void lateUpdate() {
        transform.model
                .identity()
                .translate(position)
                .rotate(rotation.x, 1, 0, 0)
                .rotate(rotation.y, 0, 1, 0)
                .rotate(rotation.z, 0, 0, 1)
                .scale(scale);
    }
}
