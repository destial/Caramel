package xyz.destiall.caramel.components;

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
        mesh.render();
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {

    }

    @Override
    public void lateUpdate() {
        mesh.transform(transform);
    }
}
