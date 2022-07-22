package caramel.api.physics.components;

import caramel.api.components.Camera;
import caramel.api.components.RigidBody2D;
import caramel.api.debug.Debug;
import caramel.api.objects.GameObject;
import org.jbox2d.collision.shapes.CircleShape;
import org.joml.Vector3f;

public final class Circle2DCollider extends Collider {
    public float radius = 0.5f;
    private transient final Vector3f debugColor = new Vector3f(0, 255, 0);

    public transient RigidBody2D rigidBody;

    public Circle2DCollider(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        rigidBody = getComponent(RigidBody2D.class);
    }

    @Override
    public void update() {
        if (rigidBody == null || rigidBody.rawBody == null) return;
        CircleShape shape = (CircleShape) rigidBody.rawBody.m_fixtureList.getShape();
        shape.setRadius(radius);
        rigidBody.rawBody.m_mass = rigidBody.mass;
    }

    @Override
    public void render(Camera camera) {
        if (collisionRender && camera.isEditor) {
            Debug.drawOutline(transform, radius, debugColor);
        }
    }
}
