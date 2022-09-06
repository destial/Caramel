package caramel.api.physics.components;

import caramel.api.components.Camera;
import caramel.api.components.RigidBody3D;
import caramel.api.debug.Debug;
import caramel.api.objects.GameObject;
import org.joml.Vector3f;
import org.ode4j.ode.DMass;

/**
 * Unused component. Was meant to be for 3D environments, but that has halted.
 */
public final class Box3DCollider extends Collider {
    public Vector3f bounds = new Vector3f(0.5f);
    private transient final Vector3f debugColor = new Vector3f(0, 255, 0);

    public transient RigidBody3D rigidBody;

    public Box3DCollider(final GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        rigidBody = getComponent(RigidBody3D.class);
    }

    @Override
    public void update() {
        if (rigidBody.rawBody == null) return;
        final DMass mass = (DMass) rigidBody.rawBody.getMass();
        mass.setMass(rigidBody.mass);
        mass.setBox(rigidBody.mass / (bounds.x * bounds.y * bounds.z), offset.x + bounds.x * 0.5f, offset.y + bounds.y * 0.5f, offset.z + bounds.z * 0.5f);
    }

    @Override
    public void render(final Camera camera) {
        if (collisionRender && camera.isEditor) {
            Debug.drawOutline(transform, bounds, debugColor);
        }
    }
}
