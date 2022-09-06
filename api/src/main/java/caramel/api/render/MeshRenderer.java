package caramel.api.render;

import caramel.api.components.Camera;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.interfaces.ShowInEditor;
import caramel.api.objects.GameObject;
import caramel.api.texture.mesh.Mesh;
import caramel.api.texture.mesh.QuadMesh;
import caramel.api.utils.Color;

import java.util.HashSet;
import java.util.Set;

public final class MeshRenderer extends Renderer {
    private static final Set<Mesh> meshes = new HashSet<>();

    public static void invalidateAll() {
        for (final Mesh mesh : meshes) {
            mesh.invalidate();
        }
        meshes.clear();
    }

    public static void addMesh(final Mesh mesh) {
        meshes.add(mesh);
    }

    @InvokeOnEdit("setColor")
    @ShowInEditor public final Color color = new Color(1f, 1f, 1f, 1f);
    public Mesh mesh;

    public MeshRenderer(final GameObject gameObject) {
        super(gameObject);
        mesh = new QuadMesh();
    }

    public MeshRenderer(final GameObject gameObject, final Mesh mesh) {
        super(gameObject);
        this.mesh = mesh;
        mesh.build();
    }

    @Override
    public void build() {
        if (mesh != null) {
            mesh.build();
        }
    }

    public void setColor() {
        if (mesh == null) return;
        mesh.setColor(color);
    }

    public void setMesh(final Mesh mesh) {
        this.mesh = mesh;
        setColor();
    }

    @Override
    public void render(final Camera camera) {
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

    @Override
    public MeshRenderer clone(final GameObject gameObject, final boolean copyId) {
        final MeshRenderer clone = (MeshRenderer) super.clone(gameObject, copyId);
        clone.mesh = mesh.copy();
        return clone;
    }
}
