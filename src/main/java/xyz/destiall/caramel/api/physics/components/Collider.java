package xyz.destiall.caramel.api.physics.components;

import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;

public abstract class Collider extends Component {
    public Vector3f offset = new Vector3f(0);

    public Collider(GameObject gameObject) {
        super(gameObject);
    }
}
