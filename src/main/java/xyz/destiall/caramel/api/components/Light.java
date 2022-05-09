package xyz.destiall.caramel.api.components;

import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;

public class Light extends Component {
    private Vector3f color;

    public Light(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {

    }
}
