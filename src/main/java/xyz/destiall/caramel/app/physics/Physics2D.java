package xyz.destiall.caramel.app.physics;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.physics.components.Box2DCollider;

public class Physics2D implements Physics {
    private final Vec2 gravity = new Vec2(0, -1f);
    private final float physicsTimeStep = 1.f / 60.f;
    private final int velocityIterations = 8;
    private final int positionInterations = 3;

    private World world = new World(gravity);

    @Override
    public void addGameObject(GameObject gameObject) {
        RigidBody2D rigidBody = gameObject.getComponent(RigidBody2D.class);
        if (rigidBody == null || rigidBody.rawBody != null) return;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(gameObject.transform.position.x, gameObject.transform.position.y);
        bodyDef.angle = 0f;
        bodyDef.angularDamping = rigidBody.angularDamping;
        bodyDef.linearDamping = rigidBody.linearDamping;
        bodyDef.fixedRotation = rigidBody.fixedRotation;
        bodyDef.bullet = rigidBody.continuousCollision;

        switch (rigidBody.bodyType) {
            case KINEMATIC:
                bodyDef.type = BodyType.KINEMATIC;
                break;
            case STATIC:
                bodyDef.type = BodyType.STATIC;
                break;
            case DYNAMIC:
                bodyDef.type = BodyType.DYNAMIC;
                break;
        }

        PolygonShape shape = new PolygonShape();

        if (gameObject.hasComponent(Box2DCollider.class)) {
            Box2DCollider collider = gameObject.getComponent(Box2DCollider.class);
            shape.setAsBox(collider.halfSize.x * 0.5f, collider.halfSize.y * 0.5f);
            Vec2 pos = bodyDef.position;
            float x = pos.x + collider.offset.x;
            float y = pos.y + collider.offset.y;
            bodyDef.position.set(x, y);
        }

        rigidBody.rawBody = world.createBody(bodyDef);
        rigidBody.rawBody.createFixture(shape, rigidBody.mass);
    }

    @Override
    public void removeGameObject(GameObject gameObject) {
        RigidBody2D rigidBody = gameObject.getComponent(RigidBody2D.class);
        if (rigidBody == null || rigidBody.rawBody == null) return;
        world.destroyBody(rigidBody.rawBody);
        rigidBody.rawBody = null;
    }

    @Override
    public void reset() {
        //physicsTime = 0;
        world = new World(gravity);
    }

    @Override
    public void update() {
        if (Time.deltaTime >= 0.f) {
            world.setAutoClearForces(true);
            world.step(physicsTimeStep, velocityIterations, positionInterations);
        }
    }
}
