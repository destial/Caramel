package xyz.destiall.caramel.api.render;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.interfaces.Render;
import xyz.destiall.caramel.api.texture.Mesh;

import java.util.LinkedList;
import java.util.List;

public abstract class Renderer extends Component implements Render {
    protected final Vector3f pos;
    protected final Quaternionf rot;
    protected final Vector3f sca;
    public Renderer(GameObject gameObject) {
        super(gameObject);
        pos = new Vector3f(transform.position);
        rot = new Quaternionf(transform.rotation);
        sca = new Vector3f(transform.scale);
    }

    @Override
    public void lateUpdate() {
        transform.model
                .identity()
                .translate(pos.set(transform.position).add(transform.localPosition))
                .rotate(rot.set(transform.rotation).add(transform.localRotation))
                .scale(sca.set(transform.scale).mul(transform.localScale));
    }
}
