package caramel.api.audio;

import caramel.api.Component;
import caramel.api.objects.GameObject;
import org.joml.Vector3f;

/**
 * This {@link Component} is used to listen to {@link AudioPlayer}s.
 */
public final class AudioListener extends Component {
    public final Vector3f offset = new Vector3f(0, 0, 0);
    public AudioListener(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {}

    @Override
    public void update() {}
}
