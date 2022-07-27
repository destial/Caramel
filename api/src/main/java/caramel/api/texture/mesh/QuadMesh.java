package caramel.api.texture.mesh;

import caramel.api.math.Vertex;
import caramel.api.texture.Mesh;

public final class QuadMesh extends Mesh {
    protected final float length;
    public QuadMesh() {
        this(1);
    }

    public QuadMesh(float length) {
        this.length = length;
        name = "Quad";
         pushVertex( 0.5f * length, -0.5f * length, 0f,   1f, 1f, 1f, 1f,   1f, 1f,   0f, 0f, 0f,   0f)
        .pushVertex(-0.5f * length,  0.5f * length, 0f,   1f, 1f, 1f, 1f,   0f, 0f,   0f, 0f, 0f,   0f)
        .pushVertex( 0.5f * length,  0.5f * length, 0f,   1f, 1f, 1f, 1f,   1f, 0f,   0f, 0f, 0f,   0f)
        .pushVertex(-0.5f * length, -0.5f * length, 0f,   1f, 1f, 1f, 1f,   0f, 1f,   0f, 0f, 0f,   0f);

        pushIndex(2, 1, 0, 0, 1, 3);
    }

    public Mesh copy() {
        QuadMesh mesh = new QuadMesh(length);
        mesh.name = name;
        mesh.drawArrays = drawArrays;
        mesh.type = type;
        mesh.texture = texture;
        mesh.shader = shader;
        mesh.dirty = dirty;
        for (Vertex vertex : vertexArray) {
            Vertex copy = new Vertex();
            copy.position.set(vertex.position);
            copy.normal.set(vertex.normal);
            copy.color.set(vertex.color);
            copy.texCoords.set(vertex.texCoords);
            copy.texSlot = vertex.texSlot;
            mesh.vertexArray.add(copy);
        }
        mesh.elementArray.addAll(elementArray);
        mesh.build(withIndices);
        return mesh;
    }
}
