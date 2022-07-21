package caramel.api.physics.components;

import caramel.api.components.Camera;
import caramel.api.components.RigidBody2D;
import caramel.api.objects.GameObject;
import org.jbox2d.collision.shapes.PolygonShape;
import org.joml.Vector2f;
import org.joml.Vector3f;
import caramel.api.debug.Debug;

public final class Box2DCollider extends Collider {
    public Vector2f bounds = new Vector2f(1f);
    public boolean useScale = false;
    private transient final Vector3f debugColor = new Vector3f(0, 255, 0);

    public transient RigidBody2D rigidBody;

    public Box2DCollider(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        rigidBody = getComponent(RigidBody2D.class);
    }

    @Override
    public void update() {
        if (rigidBody == null || rigidBody.rawBody == null) return;
        PolygonShape shape = (PolygonShape) rigidBody.rawBody.m_fixtureList.getShape();
        if (useScale) {
            shape.setAsBox(transform.scale.x * 0.5f, transform.scale.y * 0.5f);
        } else {
            shape.setAsBox(bounds.x * 0.5f, bounds.y * 0.5f);
        }
        rigidBody.rawBody.m_mass = rigidBody.mass;
    }

    @Override
    public void render(Camera camera) {
        if (collisionRender && camera.isEditor) {
            float x = useScale ? transform.scale.x : bounds.x;
            float y = useScale ? transform.scale.y : bounds.y;
            Vector3f scale = new Vector3f(x, y, 1);
            Debug.drawOutline(transform, scale, debugColor);
        }
    }
}
