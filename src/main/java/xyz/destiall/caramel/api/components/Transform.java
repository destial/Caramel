package xyz.destiall.caramel.api.components;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.interfaces.HideInEditor;

public class Transform extends Component {

    public final Vector3f position;
    public final Vector3f localPosition;
    public final Quaternionf rotation;
    public final Quaternionf localRotation;
    public final Vector3f scale;
    public final Vector3f localScale;

    @HideInEditor public transient final Matrix4f model;
    @HideInEditor public final Vector3f forward;
    @HideInEditor public final Vector3f up;

    public Transform(GameObject gameObject) {
        super(gameObject);
        gameObject.transform = this;
        position = new Vector3f();
        localPosition = new Vector3f(0, 0, 0);
        rotation = new Quaternionf();
        localRotation = new Quaternionf(0, 0, 0, 0);
        scale = new Vector3f(1, 1, 1);
        localScale = new Vector3f(1, 1, 1);
        forward = new Vector3f(0, 0, -1);
        up = new Vector3f(0, 1, 0);
        model = new Matrix4f().identity();
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
}
