package xyz.destiall.caramel.api.components;

import org.jbox2d.dynamics.Body;
import xyz.destiall.caramel.api.math.Vector2;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.physics.components.Box2DCollider;

public class RigidBody2D extends RigidBody {
    public final Vector2 velocity = new Vector2();

    public transient Body rawBody = null;
    public transient Box2DCollider collider;

    public RigidBody2D(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        collider = getComponent(Box2DCollider.class);
    }

    public void setVelocity(float x, float y) {
        velocity.set(x, y);
        if (rawBody == null) return;
        rawBody.m_linearVelocity.set(x, y);
    }

    public void addForce(Vector2 force) {
        if (rawBody == null) return;
        rawBody.applyForceToCenter(force.getJbox2d());
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public void update() {
        if (rawBody == null) return;
        transform.position.x = rawBody.getPosition().x;
        transform.position.y = rawBody.getPosition().y;
        velocity.set(rawBody.m_linearVelocity.x, rawBody.m_linearVelocity.y);
    }
}
