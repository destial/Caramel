package caramel.api.render;

import caramel.api.Component;
import caramel.api.interfaces.Render;
import caramel.api.objects.GameObject;
import org.joml.Vector3f;

public abstract class Renderer extends Component implements Render {
    protected final Vector3f pos;
    protected final Vector3f rot;
    protected final Vector3f sca;
    public Renderer(GameObject gameObject) {
        super(gameObject);
        pos = new Vector3f(transform.position);
        rot = new Vector3f(transform.rotation);
        sca = new Vector3f(transform.scale);
    }

    @Override
    public void lateUpdate() {
        rot.set(transform.rotation);
    }

    public abstract void build();
}
