package xyz.destiall.caramel.api.physics.components;

import org.joml.Vector3f;
import org.ode4j.ode.DMass;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.components.RigidBody3D;

public class Box3DCollider extends Collider {
    public Vector3f halfSize = new Vector3f(0.5f);

    public transient RigidBody3D rigidBody;

    public Box3DCollider(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        rigidBody = getComponent(RigidBody3D.class);
    }

    @Override
    public void update() {
        if (rigidBody.rawBody == null) return;
        DMass mass = (DMass) rigidBody.rawBody.getMass();
        mass.setMass(rigidBody.mass);
        mass.setBox(rigidBody.mass / (halfSize.x * halfSize.y * halfSize.z), halfSize.x * 0.5f, halfSize.y * 0.5f, halfSize.y * 0.5f);
    }
}
