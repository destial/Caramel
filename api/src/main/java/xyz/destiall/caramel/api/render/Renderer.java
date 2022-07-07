package xyz.destiall.caramel.api.render;

import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.interfaces.Render;
import xyz.destiall.caramel.api.objects.GameObject;

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
        transform.model
                .identity()
                .translate(pos.set(transform.position).add(transform.localPosition))
                .rotate(rot.x, 1, 0, 0)
                .rotate(rot.y, 0, 1, 0)
                .rotate(rot.z, 0, 0, 1)
                .scale(sca.set(transform.scale));
    }
}
