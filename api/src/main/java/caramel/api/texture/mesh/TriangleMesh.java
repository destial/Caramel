package caramel.api.texture.mesh;

import caramel.api.math.Vertex;

public final class TriangleMesh extends Mesh {
    public TriangleMesh() {
        name = "Triangle";

         pushVertex(-0.5f, -0.5f, 0f,   1f, 1f, 1f, 1f,   0f, 1f,   0f, 0f, 0f,   0f)
        .pushVertex( 0.0f,  0.5f, 0f,   1f, 1f, 1f, 1f,   0.5f, 0f,   0f, 0f, 0f,   0f)
        .pushVertex( 0.5f, -0.5f, 0f,   1f, 1f, 1f, 1f,   1f, 1f,   0f, 0f, 0f,   0f);

        pushIndex(0, 1, 2);
    }

    public Mesh copy() {
        final TriangleMesh mesh = new TriangleMesh();
        mesh.name = name;
        mesh.drawArrays = drawArrays;
        mesh.type = type;
        mesh.texture = texture;
        mesh.shader = shader;
        mesh.dirty = dirty;
        for (final Vertex vertex : vertexArray) {
            final Vertex copy = new Vertex();
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
