package xyz.destiall.caramel.api.components;

import org.ode4j.ode.DBody;
import xyz.destiall.caramel.api.math.Vector3;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.physics.components.Box3DCollider;

public class RigidBody3D extends RigidBody {
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
                    (float) rawBody.getRotation().get02(),
                    transform.rotation.w
            );
        }
    }

    public Vector3 getVelocity() {
        return velocity;
    }
}
