package caramel.api.texture.mesh;

import caramel.api.math.Vertex;

public class CubeMesh extends Mesh {
    protected final float length;
    public CubeMesh() {
        this(1);
    }

    public CubeMesh(final float length) {
        this.length = length;
        name = "Cube";
         pushVertex(-0.5f * length,  0.5f * length, 0.5f * length,   1f, 1f, 1f, 1f,   1f, 1f,   0f, 0f, 0f,   0f)
        .pushVertex( 0.5f * length,  0.5f * length, 0.5f * length,   1f, 1f, 1f, 1f,   0f, 0f,   0f, 0f, 0f,   0f)
        .pushVertex(-0.5f * length, -0.5f * length, 0.5f * length,   1f, 1f, 1f, 1f,   1f, 0f,   0f, 0f, 0f,   0f)
        .pushVertex( 0.5f * length, -0.5f * length, 0.5f * length,   1f, 1f, 1f, 1f,   0f, 1f,   0f, 0f, 0f,   0f)

        .pushVertex(-0.5f * length,  0.5f * length, -0.5f * length,   1f, 1f, 1f, 1f,   1f, 1f,   0f, 0f, 0f,   0f)
        .pushVertex( 0.5f * length,  0.5f * length, -0.5f * length,   1f, 1f, 1f, 1f,   0f, 0f,   0f, 0f, 0f,   0f)
        .pushVertex(-0.5f * length, -0.5f * length, -0.5f * length,   1f, 1f, 1f, 1f,   1f, 0f,   0f, 0f, 0f,   0f)
        .pushVertex( 0.5f * length, -0.5f * length, -0.5f * length,   1f, 1f, 1f, 1f,   0f, 1f,   0f, 0f, 0f,   0f);

        pushIndex(0, 1, 2, // 0
                1, 3, 2,
                4, 6, 5, // 2
                5, 6, 7,
                0, 2, 4, // 4
                4, 2, 6,
                1, 5, 3, // 6
                5, 7, 3,
                0, 4, 1, // 8
                4, 5, 1,
                2, 3, 6, // 10
                6, 3, 7);
    }

    public Mesh copy() {
        final CubeMesh mesh = new CubeMesh(length);
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
