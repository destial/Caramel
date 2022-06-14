package xyz.destiall.caramel.api.components;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import xyz.destiall.caramel.api.math.Vector2;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.physics.components.Box2DCollider;
import xyz.destiall.caramel.api.physics.info.RaycastInfo2D;

public class RigidBody2D extends RigidBody {
    public final Vector2 velocity = new Vector2();

    public transient Body rawBody = null;
    public transient Box2DCollider collider;

    public RigidBody2D(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void _setPosition(float x, float y, float z) {
        if (rawBody == null) return;
        rawBody.setTransform(new Vec2(x, y), rawBody.getAngle());
        // transform.setPosition(x, y, z);
    }

    @Override
    public void start() {
        collider = getComponent(Box2DCollider.class);
    }

    public void setVelocity(float x, float y) {
        this.velocity.set(x, y);
        if (rawBody == null) return;
        rawBody.setLinearVelocity(velocity.getJbox2d());
    }

    public void setVelocity(Vector2 vel) {
        this.velocity.set(vel.x(), vel.y());
        if (rawBody == null) return;
        rawBody.setLinearVelocity(velocity.getJbox2d());
    }

    public void addVelocity(float x, float y) {
        if (rawBody == null) return;
        Vec2 vel = rawBody.getLinearVelocity();
        rawBody.setLinearVelocity(vel.addLocal(x, y));
    }

    public void addForce(float x, float y) {
        if (rawBody == null) return;
        rawBody.applyForceToCenter(new Vec2(x, y));
    }

    public void addForce(Vector2 force) {
        if (rawBody == null) return;
        rawBody.applyForceToCenter(force.getJbox2d());
    }

    public void setForce(Vector2 force) {
        if (rawBody == null) return;
        rawBody.m_force.set(force.x(), force.y());
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Vector2 getForce() {
        if (rawBody == null) return new Vector2();
        return new Vector2(rawBody.m_force);
    }

    public boolean isOnGround() {
        if (rawBody == null || collider == null) return false;
        Vec2 raycastStart = new Vec2(transform.position.x, transform.position.y);
        raycastStart.y -= collider.useScale ? transform.scale.y * 0.5f : collider.halfSize.y * 0.5f;
        Vec2 raycastEnd = new Vec2(raycastStart);
        raycastEnd.y -= 0.1f;
        RaycastInfo2D raycastInfo = new RaycastInfo2D(gameObject);
        rawBody.m_world.raycast(raycastInfo, raycastStart, raycastEnd);
        return raycastInfo.hit;
    }

    @Override
    public void lateUpdate() {
        if (rawBody == null) return;
        transform.position.x = rawBody.getTransform().p.x;
        transform.position.y = rawBody.getTransform().p.y;
        velocity.set(rawBody.m_linearVelocity.x, rawBody.m_linearVelocity.y);
    }
}
