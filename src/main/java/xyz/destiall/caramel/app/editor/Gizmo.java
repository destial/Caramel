package xyz.destiall.caramel.app.editor;

import org.joml.Vector4f;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.mesh.Mesh;
import xyz.destiall.caramel.api.mesh.MeshBuilder;
import xyz.destiall.caramel.interfaces.Render;

public class Gizmo implements Render {
    private final Mesh mesh;
    private GameObject target;

    public Gizmo() {
        mesh = MeshBuilder.createCircle(new Vector4f(1f, 0f, 0f, 1f), 0.1f, 18);
        mesh.build();
    }

    public void setTarget(GameObject target) {
        this.target = target;
    }

    @Override
    public void render(Camera camera) {
        if (target == null) return;
        mesh.render(target.transform, camera);
    }
}
