package xyz.destiall.caramel.api.physics.components;

import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.interfaces.Render;

public abstract class Collider extends Component implements Render {
    public Vector3f offset = new Vector3f(0);
    public boolean collisionRender = true;

    public Collider(GameObject gameObject) {
        super(gameObject);
    }
}
