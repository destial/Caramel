package caramel.api.components;

import caramel.api.Component;
import org.joml.Vector3f;
import caramel.api.objects.GameObject;

public final class Light extends Component {
    private Vector3f color;

    public Light(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {}
}
