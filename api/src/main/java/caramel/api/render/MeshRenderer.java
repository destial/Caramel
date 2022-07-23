package caramel.api.render;

import caramel.api.components.Camera;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.interfaces.ShowInEditor;
import caramel.api.objects.GameObject;
import caramel.api.texture.Mesh;
import caramel.api.texture.MeshBuilder;
import caramel.api.utils.Color;

public final class MeshRenderer extends Renderer {
    public Mesh mesh;

    @ShowInEditor
    @InvokeOnEdit("setColor")
    public Color color = new Color(1f, 1f, 1f, 1f);

    public MeshRenderer(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void build() {
        if (mesh != null) mesh.build();
    }

    public void setColor() {
        mesh.setColor(color);
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
        setColor();
    }

    @Override
    public void render(Camera camera) {
        if (mesh == null) {
            mesh = MeshBuilder.createQuad(1);
            setColor();
            mesh.build();
        }

        if (mesh != null) {
            if (BatchRenderer.USE_BATCH) {
                mesh.renderBatch(transform, camera);
            } else {
                mesh.render(transform, camera);
            }
        }
    }

    @Override
    public void start() {}
}
