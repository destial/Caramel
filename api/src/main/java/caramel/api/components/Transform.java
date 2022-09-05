package caramel.api.components;

import caramel.api.Component;
import caramel.api.interfaces.HideInEditor;
import caramel.api.objects.GameObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * This {@link Component} represents the transform matrices of the {@link GameObject}.
 */
public final class Transform extends Component {
    public final Vector3f position;
    public final Vector3f localPosition;
    public final Vector3f rotation;
    public final Vector3f scale;

    @HideInEditor private transient final Matrix4f model;
    @HideInEditor public final Vector3f forward;
    @HideInEditor public final Vector3f up;

    public Transform(final GameObject gameObject) {
        super(gameObject);
        gameObject.transform = this;
        transform = this;
        position = new Vector3f(1f, 1f, -1f);
        localPosition = new Vector3f(0f, 0f, 0f);
        rotation = new Vector3f(0f, 0f, 0f);
        scale = new Vector3f(1f, 1f, 1f);

        forward = new Vector3f(0f, 0f, -1f);
        up = new Vector3f(0f, 1f, 0f);
        model = new Matrix4f().identity();
        gameObject.addComponent(this);
    }

    public void setPosition(float x, float y, float z) {
        final Vector3f pos = gameObject.parent != null ? localPosition : position;
        pos.set(x, y, z);
        RigidBody rb = getComponent(RigidBody.class);
        if (rb != null) {
            rb._setPosition(position.x + localPosition.x, position.y + localPosition.y, position.z + localPosition.z);
        }
    }

    public Matrix4f getModel() {
        final Vector3f pos = new Vector3f(position).add(localPosition);
        model
            .identity()
            .translate(pos)
            .rotate(rotation.x, 1, 0, 0)
            .rotate(rotation.y, 0, 1, 0)
            .rotate(rotation.z, 0, 0, 1)
            .scale(scale);

        return model;
    }

    @Override
    public void start() {}

    public void update() {}

}
