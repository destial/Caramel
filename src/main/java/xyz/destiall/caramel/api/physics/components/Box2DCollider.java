package xyz.destiall.caramel.api.physics.components;

import org.jbox2d.collision.shapes.PolygonShape;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.app.editor.EditorCamera;

public class Box2DCollider extends Collider {
    public Vector2f halfSize = new Vector2f(1f);
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
        if (rigidBody.rawBody == null) return;
        PolygonShape shape = (PolygonShape) rigidBody.rawBody.m_fixtureList.getShape();
        shape.setAsBox(halfSize.x * 0.5f, halfSize.y * 0.5f);
        rigidBody.rawBody.m_mass = rigidBody.mass;
    }

    @Override
    public void render(Camera camera) {
        if (collisionRender && camera instanceof EditorCamera) {
            Debug.drawLine(
                    new Vector3f(transform.position.x - halfSize.x * 0.5f, transform.position.y + halfSize.y * 0.5f, transform.position.z),
                    new Vector3f(transform.position.x + halfSize.x * 0.5f, transform.position.y + halfSize.y * 0.5f, transform.position.z),
                    debugColor
            );
            Debug.drawLine(
                    new Vector3f(transform.position.x - halfSize.x * 0.5f, transform.position.y - halfSize.y * 0.5f, transform.position.z),
                    new Vector3f(transform.position.x - halfSize.x * 0.5f, transform.position.y + halfSize.y * 0.5f, transform.position.z),
                    debugColor
            );
            Debug.drawLine(
                    new Vector3f(transform.position.x - halfSize.x * 0.5f, transform.position.y - halfSize.y * 0.5f, transform.position.z),
                    new Vector3f(transform.position.x + halfSize.x * 0.5f, transform.position.y - halfSize.y * 0.5f, transform.position.z),
                    debugColor
            );
            Debug.drawLine(
                    new Vector3f(transform.position.x + halfSize.x * 0.5f, transform.position.y - halfSize.y * 0.5f, transform.position.z),
                    new Vector3f(transform.position.x + halfSize.x * 0.5f, transform.position.y + halfSize.y * 0.5f, transform.position.z),
                    debugColor
            );
        }
    }
}
