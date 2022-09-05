package caramel.api.texture.mesh;

import caramel.api.math.Vertex;

import static org.jbox2d.common.MathUtils.TWOPI;

public final class CircleMesh extends Mesh {
    private final float radius;
    private final int sides;
    public CircleMesh() {
        this(0.5f, 36);
    }
    public CircleMesh(final float radius, final int n) {
        name = "Circle";
        this.radius = radius;
        this.sides = n;
        pushVertex(0f, 0f, 0f,   1f, 1f, 1f, 1f,   0.5f, 0.5f,   0f, 0f, 1f,   0f);

        for (int i = 0; i <= n; i++) {
            final float x = (float) (radius * Math.sin(TWOPI * i / n));
            final float y = (float) (radius * Math.cos(TWOPI * i / n));
            pushVertex(x, y, 0f,   1f, 1f, 1f, 1f,   ((x / radius) + 1) / 2f, 1 - ((y / radius) + 1) / 2f,   0f, 0f, 1f,   0f);
        }

        for (int i = 0; i < n; i++) {
            pushIndex(0).pushIndex(i + 1).pushIndex(i + 2);
        }
    }

    @Override
    public Mesh copy() {
        final CircleMesh mesh = new CircleMesh(radius, sides);
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
