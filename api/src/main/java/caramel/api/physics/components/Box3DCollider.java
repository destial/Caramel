package caramel.api.physics.components;

import caramel.api.components.Camera;
import caramel.api.components.RigidBody3D;
import caramel.api.debug.Debug;
import caramel.api.objects.GameObject;
import org.joml.Vector3f;
import org.ode4j.ode.DMass;

public final class Box3DCollider extends Collider {
    public Vector3f halfSize = new Vector3f(0.5f);
    private transient final Vector3f debugColor = new Vector3f(0, 255, 0);

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
        mass.setBox(rigidBody.mass / (halfSize.x * halfSize.y * halfSize.z), offset.x + halfSize.x * 0.5f, offset.y + halfSize.y * 0.5f, offset.z + halfSize.y * 0.5f);
    }

    @Override
    public void render(Camera camera) {
        if (collisionRender && camera.isEditor) {
            Debug.drawOutline(transform, halfSize, debugColor);
            Debug.drawLine(
                    new Vector3f(offset.x + transform.position.x - halfSize.x * 0.5f, offset.y + transform.position.y + halfSize.y * 0.5f, transform.position.z),
                    new Vector3f(offset.x + transform.position.x + halfSize.x * 0.5f, offset.y + transform.position.y + halfSize.y * 0.5f, transform.position.z),
                    debugColor
            );
            Debug.drawLine(
                    new Vector3f(offset.x + transform.position.x - halfSize.x * 0.5f, offset.y + transform.position.y - halfSize.y * 0.5f, transform.position.z),
                    new Vector3f(offset.x + transform.position.x - halfSize.x * 0.5f, offset.y + transform.position.y + halfSize.y * 0.5f, transform.position.z),
                    debugColor
            );
            Debug.drawLine(
                    new Vector3f(offset.x + transform.position.x - halfSize.x * 0.5f, offset.y + transform.position.y - halfSize.y * 0.5f, transform.position.z),
                    new Vector3f(offset.x + transform.position.x + halfSize.x * 0.5f, offset.y + transform.position.y - halfSize.y * 0.5f, transform.position.z),
                    debugColor
            );
            Debug.drawLine(
                    new Vector3f(offset.x + transform.position.x + halfSize.x * 0.5f, offset.y + transform.position.y - halfSize.y * 0.5f, transform.position.z),
                    new Vector3f(offset.x + transform.position.x + halfSize.x * 0.5f, offset.y + transform.position.y + halfSize.y * 0.5f, transform.position.z),
                    debugColor
            );
        }
    }
}