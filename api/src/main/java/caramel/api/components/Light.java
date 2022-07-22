package caramel.api.components;

import caramel.api.Component;
import caramel.api.objects.GameObject;
import org.joml.Vector3f;

public final class Light extends Component {
    private Vector3f color;

    public Light(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {}
}
