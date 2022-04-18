package xyz.destiall.caramel.editor;

import org.joml.Vector4f;
import xyz.destiall.caramel.graphics.Mesh;
import xyz.destiall.caramel.graphics.MeshBuilder;
import xyz.destiall.caramel.interfaces.Render;
import xyz.destiall.caramel.objects.GameObject;

public class Gizmo implements Render {
    private final Mesh mesh;
    private GameObject target;

    public Gizmo() {
        mesh = MeshBuilder.createCircle(new Vector4f(1f, 1f, 1f, 1f), 0.1f, 18);
        mesh.build();
    }

    public void setTarget(GameObject target) {
        this.target = target;
    }

    @Override
    public void render() {
        if (target == null) return;
        mesh.render(target.transform);
    }
}
