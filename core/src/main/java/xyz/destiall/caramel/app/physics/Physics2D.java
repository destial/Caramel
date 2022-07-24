package xyz.destiall.caramel.app.physics;

import caramel.api.Time;
import caramel.api.components.RigidBody2D;
import caramel.api.objects.GameObject;
import caramel.api.objects.SceneImpl;
import caramel.api.physics.components.Box2DCollider;
import caramel.api.physics.components.Circle2DCollider;
import caramel.api.physics.internals.ContactListener;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;


public final class Physics2D implements Physics {
    private final Vec2 gravity = new Vec2(0, -10f);

    private World world;
    private final SceneImpl scene;

    public Physics2D(SceneImpl scene) {
        this.scene = scene;
        reset();
    }

    @Override
    public void addGameObject(GameObject gameObject) {
        RigidBody2D rigidBody = gameObject.getComponent(RigidBody2D.class);
        if (rigidBody == null || rigidBody.rawBody != null) return;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(gameObject.transform.position.x, gameObject.transform.position.y);
        bodyDef.angle = gameObject.transform.rotation.z;
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

        Shape shape = null;

        if (gameObject.hasComponent(Box2DCollider.class)) {
            shape = new PolygonShape();
            Box2DCollider collider = gameObject.getComponent(Box2DCollider.class);
            ((PolygonShape) shape).setAsBox(collider.useScale ? rigidBody.transform.scale.x * 0.5f : collider.bounds.x * 0.5f, collider.useScale ? rigidBody.transform.scale.y * 0.5f : collider.bounds.y * 0.5f);
            Vec2 pos = bodyDef.position;
            float x = pos.x + collider.offset.x;
            float y = pos.y + collider.offset.y;
            bodyDef.position.set(x, y);

        } else if (gameObject.hasComponent(Circle2DCollider.class)) {
            shape = new CircleShape();
            Circle2DCollider collider = gameObject.getComponent(Circle2DCollider.class);
            shape.setRadius(collider.radius);
            Vec2 pos = bodyDef.position;
            float x = pos.x + collider.offset.x;
            float y = pos.y + collider.offset.y;
            bodyDef.position.set(x, y);
        }

        rigidBody.rawBody = world.createBody(bodyDef);
        Fixture fixture = rigidBody.rawBody.createFixture(shape, rigidBody.mass);
        fixture.setSensor(rigidBody.isTrigger);
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
        world = new World(gravity);
        world.setContactListener(new ContactListener(scene));
        world.setAllowSleep(true);
        world.setAutoClearForces(true);
        world.setSubStepping(true);
    }

    @Override
    public void invalidate() {
        if (world == null) return;
        world.clearForces();
        world = null;
    }

    @Override
    public void update() {
        if (world != null && Time.deltaTime >= 0.f) {
            world.step(Time.deltaTime, 3, 3);
        }
    }
}
