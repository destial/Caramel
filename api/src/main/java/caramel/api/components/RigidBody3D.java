package caramel.api.components;

import org.ode4j.ode.DBody;
import caramel.api.math.Vector3;
import caramel.api.objects.GameObject;
import caramel.api.physics.components.Box3DCollider;

public final class RigidBody3D extends RigidBody {
    public final Vector3 velocity = new Vector3();

    public DBody rawBody = null;
    public Box3DCollider collider;

    public RigidBody3D(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void _setPosition(float x, float y, float z) {

    }

    @Override
    public void start() {
        collider = getComponent(Box3DCollider.class);
    }

    @Override
    public void update() {
        if (rawBody != null) {
            velocity.set(
                    rawBody.getLinearVel().get0(),
                    rawBody.getLinearVel().get1(),
                    rawBody.getLinearVel().get2()
            );
            transform.position.set(
                    rawBody.getPosition().get0(),
                    rawBody.getPosition().get1(),
                    rawBody.getPosition().get2()
            );
            transform.rotation.set(
                    (float) rawBody.getRotation().get00(),
                    (float) rawBody.getRotation().get01(),
                    (float) rawBody.getRotation().get02()
            );
        }
    }

    public Vector3 getVelocity() {
        return velocity;
    }
}
