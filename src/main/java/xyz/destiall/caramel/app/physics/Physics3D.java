package xyz.destiall.caramel.app.physics;

import org.ode4j.math.DVector3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.api.components.RigidBody3D;
import xyz.destiall.caramel.api.physics.components.Box3DCollider;
import xyz.destiall.caramel.app.editor.Scene;

public class Physics3D implements Physics {
    private final DVector3 gravity = new DVector3(0, -1f, 0);
    private final float physicsTimeStep = 1.f / 60.f;

    private DWorld world;
    private final Scene scene;

    public Physics3D(Scene scene) {
        this.scene = scene;
        world = OdeHelper.createWorld();
        world.setGravity(gravity);
    }

    @Override
    public void addGameObject(GameObject gameObject) {
        RigidBody3D rigidBody = gameObject.getComponent(RigidBody3D.class);
        if (rigidBody == null || rigidBody.rawBody != null) return;

        DBody bodyDef = OdeHelper.createBody(world);
        DMass mass = OdeHelper.createMass();
        bodyDef.setMass(mass);
        mass.setMass(rigidBody.mass);
        bodyDef.setPosition(gameObject.transform.position.x, gameObject.transform.position.y, gameObject.transform.position.z);
        bodyDef.setAngularDamping(rigidBody.angularDamping);
        bodyDef.setLinearDamping(rigidBody.linearDamping);
        bodyDef.setFiniteRotationMode(rigidBody.fixedRotation);

        switch (rigidBody.bodyType) {
            case KINEMATIC:
                bodyDef.setKinematic();
                break;
            case STATIC:
                mass.setMass(10000000000d);
                break;
            case DYNAMIC:
                bodyDef.setDynamic();
                break;
        }

        if (gameObject.hasComponent(Box3DCollider.class)) {
            Box3DCollider collider = gameObject.getComponent(Box3DCollider.class);
            DBox box = OdeHelper.createBox(collider.halfSize.x * 0.5f, collider.halfSize.y * 0.5f, collider.halfSize.z * 0.5f);
            box.setBody(bodyDef);
            mass.setBox(rigidBody.mass / (collider.halfSize.x * collider.halfSize.y * collider.halfSize.z), collider.halfSize.x * 0.5f, collider.halfSize.y * 0.5f, collider.halfSize.z * 0.5f);
        }

        rigidBody.rawBody = bodyDef;
    }

    @Override
    public void removeGameObject(GameObject gameObject) {
        RigidBody3D rigidBody = gameObject.getComponent(RigidBody3D.class);
        if (rigidBody == null || rigidBody.rawBody == null) return;
        rigidBody.rawBody.destroy();
        rigidBody.rawBody = null;
    }

    @Override
    public void reset() {
        world.destroy();
        world = OdeHelper.createWorld();
        world.setGravity(gravity);
    }

    @Override
    public void update() {
        if (Time.deltaTime >= 0.f) {
            world.step(physicsTimeStep);
        }
    }
}
