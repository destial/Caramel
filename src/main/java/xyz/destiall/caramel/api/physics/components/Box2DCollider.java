package xyz.destiall.caramel.api.physics.components;

import org.jbox2d.collision.shapes.PolygonShape;
import org.joml.Vector2f;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.components.RigidBody2D;

public class Box2DCollider extends Collider {
    public Vector2f halfSize = new Vector2f(0.5f);

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
    }
}
