package xyz.destiall.caramel.app.physics;

import caramel.api.Time;
import caramel.api.components.RigidBody3D;
import caramel.api.objects.GameObject;
import caramel.api.objects.SceneImpl;
import caramel.api.physics.components.Box3DCollider;
import org.joml.Quaternionf;
import org.ode4j.math.DQuaternion;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

public final class Physics3D implements Physics {
    private final DVector3 gravity = new DVector3(0, -98f, 0);

    private DWorld world;
    private final SceneImpl scene;

    public Physics3D(SceneImpl scene) {
        this.scene = scene;
        world = OdeHelper.createWorld();
        world.setGravity(gravity);
    }

    // @SuppressWarnings("all")
    @Override
    public void addGameObject(GameObject gameObject) {
        RigidBody3D rigidBody = gameObject.getComponent(RigidBody3D.class);
        if (rigidBody == null || rigidBody.rawBody != null) return;

        DBody bodyDef = OdeHelper.createBody(world);
        DMass mass = OdeHelper.createMass();
        mass.setMass(rigidBody.mass);
        bodyDef.setPosition(gameObject.transform.position.x, gameObject.transform.position.y, gameObject.transform.position.z);
        Quaternionf rot = gameObject.transform.rotation.rotationTo(gameObject.transform.rotation, new Quaternionf());
        DQuaternion quat = new DQuaternion();
        quat.set(rot.x, rot.y, rot.z, rot.w);
        bodyDef.setQuaternion(quat);
        bodyDef.setAngularDamping(rigidBody.angularDamping);
        bodyDef.setLinearDamping(rigidBody.linearDamping);
        bodyDef.setFiniteRotationMode(rigidBody.fixedRotation);

        switch (rigidBody.bodyType) {
            case KINEMATIC:
                bodyDef.setKinematic();
                break;
            case STATIC:
                bodyDef.setGravityMode(false);
                mass.setMass(1000000d);
                break;
            case DYNAMIC:
                bodyDef.setDynamic();
                break;
        }

        if (gameObject.hasComponent(Box3DCollider.class)) {
            Box3DCollider collider = gameObject.getComponent(Box3DCollider.class);
            DBox box = OdeHelper.createBox(collider.bounds.x * 0.5f, collider.bounds.y * 0.5f, collider.bounds.z * 0.5f);
            box.setBody(bodyDef);
            mass.setBox(rigidBody.mass / (collider.bounds.x * collider.bounds.y * collider.bounds.z), collider.bounds.x * 0.5f, collider.bounds.y * 0.5f, collider.bounds.z * 0.5f);
        }

        bodyDef.setMass(mass);
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
    public void invalidate() {
        if (world == null) return;
        world.destroy();
        world = null;
    }

    @Override
    public void update() {
        if (world != null && Time.deltaTime >= 0.f) {
            world.step(Time.deltaTime);
        }
    }
}
