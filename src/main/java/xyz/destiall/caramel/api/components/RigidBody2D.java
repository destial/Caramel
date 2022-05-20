package xyz.destiall.caramel.api.components;

import org.jbox2d.dynamics.Body;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.physics.components.Box2DCollider;
import xyz.destiall.caramel.api.physics.RigidBodyType;

public class RigidBody2D extends Component {
    public final Vector2f velocity = new Vector2f();
    public float angularDamping = 0.8f;
    public float linearDamping = 0.9f;
    public float mass = 0f;
    public RigidBodyType bodyType = RigidBodyType.DYNAMIC;

    public boolean fixedRotation = false;
    public boolean continuousCollision = true;

    public transient Body rawBody = null;
    public transient Box2DCollider collider;

    public RigidBody2D(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        collider = getComponent(Box2DCollider.class);
    }

    @Override
    public void update() {
        if (rawBody != null) {
            transform.position.x = rawBody.getPosition().x;
            transform.position.y = rawBody.getPosition().y;
            velocity.set(rawBody.m_linearVelocity.x, rawBody.m_linearVelocity.y);

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
