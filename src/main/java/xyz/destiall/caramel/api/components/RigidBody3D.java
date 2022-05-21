package xyz.destiall.caramel.api.components;

import org.joml.Vector3f;
import org.ode4j.ode.DBody;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.physics.RigidBodyType;
import xyz.destiall.caramel.api.physics.components.Box3DCollider;

public class RigidBody3D extends Component {
    public final Vector3f velocity = new Vector3f();
    public float angularDamping = 0.8f;
    public float linearDamping = 0.9f;
    public float mass = 0f;
    public RigidBodyType bodyType = RigidBodyType.DYNAMIC;

    public boolean fixedRotation = false;
    public DBody rawBody = null;
    public Box3DCollider collider;

    public RigidBody3D(GameObject gameObject) {
        super(gameObject);
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

            Debug.drawBox(new Vector3f(
                            transform.position.x - (collider.halfSize.x * 0.5f),
                            transform.position.y - (collider.halfSize.y * 0.5f),
                            transform.position.z - (transform.scale.z * 0.5f)),
                    new Vector3f(
                            transform.position.x + (collider.halfSize.x * 0.5f),
                            transform.position.y + (collider.halfSize.y * 0.5f),
                            transform.position.z + (transform.scale.z * 0.5f)),
                    new Vector3f(0, 255, 0)
            );
        }
    }
}
