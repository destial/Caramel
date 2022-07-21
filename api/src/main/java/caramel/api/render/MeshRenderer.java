package caramel.api.render;

import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.interfaces.ShowInEditor;
import caramel.api.utils.Color;
import caramel.api.components.Camera;
import caramel.api.objects.GameObject;
import caramel.api.texture.Mesh;
import caramel.api.texture.MeshBuilder;

public final class MeshRenderer extends Renderer {
    public Mesh mesh;

    @ShowInEditor
    @InvokeOnEdit("setColor")
    public Color color = new Color(1f, 1f, 1f, 1f);

    public MeshRenderer(GameObject gameObject) {
        super(gameObject);
    }

    public void setColor() {
        mesh.setColor(color);
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public void render(Camera camera) {
        if (mesh == null) {
            mesh = MeshBuilder.createQuad(1);
            mesh.setColor(color);
            mesh.build();
        }

        if (mesh != null) mesh.render(transform, camera);
    }

    @Override
    public void start() {}
}
