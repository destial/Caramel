package caramel.api.components;

import caramel.api.math.Vector3;
import caramel.api.objects.GameObject;
import org.joml.Quaternionf;
import org.ode4j.math.DMatrix3C;
import org.ode4j.ode.DBody;

/**
 * Unused component. Was meant to be for 3D environments, but that has halted.
 */
public final class RigidBody3D extends RigidBody {
    public final Vector3 velocity = new Vector3();

    public transient DBody rawBody = null;

    public RigidBody3D(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void _setPosition(float x, float y, float z) {

    }

    @Override
    protected void updateBody() {

    }

    @Override
    public void start() {
    }

    @Override
    public void lateUpdate() {
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

            DMatrix3C rot = rawBody.getRotation();
            double b1_squared = 0.25 * (1.f + rot.get00() + rot.get11() + rot.get22());
            if (b1_squared >= 0.25) {
                double b1 = Math.sqrt(b1_squared);

                double over_b1_4 = 0.25 / b1;
                double b2 = (rot.get21() - rot.get12()) * over_b1_4;
                double b3 = (rot.get02() - rot.get20()) * over_b1_4;
                double b4 = (rot.get10() - rot.get01()) * over_b1_4;
                Quaternionf quaternionf = new Quaternionf(b1, b2, b3, b4);
                transform.rotation.rotate(quaternionf);
            }
        }
    }

    public Vector3 getVelocity() {
        return velocity;
    }
}
