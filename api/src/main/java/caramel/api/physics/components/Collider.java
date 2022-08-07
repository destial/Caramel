package caramel.api.physics.components;

import caramel.api.Component;
import caramel.api.interfaces.Render;
import caramel.api.objects.GameObject;
import org.joml.Vector3f;

/**
 * This {@link Component} is for collision abstraction.
 */
public abstract class Collider extends Component implements Render {
    public Vector3f offset = new Vector3f(0);
    public boolean collisionRender = false;

    public Collider(GameObject gameObject) {
        super(gameObject);
    }
}
