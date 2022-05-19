package xyz.destiall.caramel.api.components;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.mesh.Mesh;
import xyz.destiall.caramel.interfaces.Render;

public class MeshRenderer extends Component implements Render {

    private final Vector3f pos;
    private final Quaternionf rot;
    private final Vector3f sca;

    public Mesh mesh;

    public MeshRenderer(GameObject gameObject) {
        super(gameObject);
        pos = new Vector3f(transform.position);
        rot = new Quaternionf(transform.rotation);
        sca = new Vector3f(transform.scale);
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public void render() {
        if (mesh != null) mesh.render(transform);
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {

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
