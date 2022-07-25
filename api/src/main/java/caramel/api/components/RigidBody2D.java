package caramel.api.components;

import caramel.api.math.Vector2;
import caramel.api.objects.GameObject;
import caramel.api.physics.components.Box2DCollider;
import caramel.api.physics.components.Circle2DCollider;
import caramel.api.physics.info.RaycastInfo2D;
import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;

public final class RigidBody2D extends RigidBody {
    public final Vector2 velocity = new Vector2();

    public transient Body rawBody = null;
    public transient Box2DCollider box2DCollider;
    public transient Circle2DCollider circle2DCollider;

    public transient Fixture fixture;

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
    protected void updateBody() {
        if (fixture == null || rawBody == null) return;
        rawBody.setAngularDamping(angularDamping);
        rawBody.setLinearDamping(linearDamping);
        rawBody.setFixedRotation(fixedRotation);
        rawBody.setBullet(continuousCollision);
        fixture.setSensor(isTrigger);
        fixture.setFriction(friction);
        switch (bodyType) {
            case KINEMATIC:
                rawBody.setType(BodyType.KINEMATIC);
                break;
            case STATIC:
                rawBody.setType(BodyType.STATIC);
                break;
            case DYNAMIC:
                rawBody.setType(BodyType.DYNAMIC);
                break;
        }
        MassData massData = new MassData();
        fixture.getMassData(massData);
        massData.mass = mass;
        rawBody.setMassData(massData);
    }

    @Override
    public void start() {
        box2DCollider = getComponent(Box2DCollider.class);
        circle2DCollider = getComponent(Circle2DCollider.class);
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
        if (rawBody != null) {
            velocity.set(rawBody.m_linearVelocity.x, rawBody.m_linearVelocity.y);
        }
        return velocity;
    }

    public Vector2 getForce() {
        if (rawBody == null) return new Vector2();
        return new Vector2(rawBody.m_force);
    }

    public boolean isOnGround() {
        if (rawBody == null) return false;
        Vec2 raycastStart = new Vec2(transform.position.x, transform.position.y);
        if (box2DCollider != null) {
            raycastStart.y -= box2DCollider.useScale ? transform.scale.y * 0.5f : box2DCollider.bounds.y * 0.5f;
        } else if (circle2DCollider != null) {
            raycastStart.y -= circle2DCollider.radius;
        }
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
        transform.rotation.z = rawBody.getTransform().q.getAngle();
        velocity.set(rawBody.m_linearVelocity.x, rawBody.m_linearVelocity.y);
    }
}
