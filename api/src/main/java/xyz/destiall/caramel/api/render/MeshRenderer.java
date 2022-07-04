package xyz.destiall.caramel.api.render;

import org.joml.Vector4f;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.interfaces.FunctionButton;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.texture.Mesh;
import xyz.destiall.caramel.api.texture.MeshBuilder;

public final class MeshRenderer extends Renderer {
    public Mesh mesh;

    public MeshRenderer(GameObject gameObject) {
        super(gameObject);
        mesh = MeshBuilder.createQuad(1);
        mesh.setColor(new Vector4f(1f, 1f, 1f, 1f));
        mesh.build();
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    @FunctionButton
    public void createMesh() {
        Debug.log("mesh boo");
    }

    @Override
    public void render(Camera camera) {
        if (mesh != null) mesh.render(transform, camera);
    }

    @Override
    public void start() {}
}
