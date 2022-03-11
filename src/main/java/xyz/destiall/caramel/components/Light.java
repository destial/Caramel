package xyz.destiall.caramel.components;

import org.joml.Vector3f;
import xyz.destiall.caramel.objects.GameObject;

public class Light extends Component {
    private Vector3f color;

    public Light(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {

    }
}
