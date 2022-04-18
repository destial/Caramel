package xyz.destiall.caramel.components;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.destiall.caramel.graphics.Mesh;
import xyz.destiall.caramel.interfaces.HideInEditor;
import xyz.destiall.caramel.interfaces.Render;
import xyz.destiall.caramel.objects.GameObject;

public class MeshRenderer extends Component implements Render {
    @HideInEditor private Transform last;

    public Mesh mesh;

    public MeshRenderer(GameObject gameObject) {
        super(gameObject);
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public void render() {
        transform.model.identity();
        transform.model.translate(new Vector3f(transform.position).add(transform.localPosition))
                .rotate(new Quaternionf(transform.rotation).add(transform.localRotation))
                .scale(new Vector3f(transform.scale).mul(transform.localScale));
        mesh.render(transform);
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {

    }

    @Override
    public void lateUpdate() {

    }
}
