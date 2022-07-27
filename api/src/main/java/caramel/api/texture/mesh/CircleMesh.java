package caramel.api.texture.mesh;

import caramel.api.math.Vertex;
import caramel.api.texture.Mesh;

import static org.jbox2d.common.MathUtils.TWOPI;

public final class CircleMesh extends Mesh {
    protected final float radius;
    protected final int sides;
    public CircleMesh() {
        this(0.5f, 36);
    }
    public CircleMesh(float radius, int n) {
        name = "Circle";
        this.radius = radius;
        this.sides = n;
        pushVertex(0f, 0f, 0f,   1f, 1f, 1f, 1f,   0.5f, 0.5f,   0f, 0f, 1f,   0f);

        for (int i = 0; i <= n; i++) {
            float x = (float) (radius * Math.sin(TWOPI * i / n));
            float y = (float) (radius * Math.cos(TWOPI * i / n));
            pushVertex(x, y, 0f,   1f, 1f, 1f, 1f,   ((x / radius) + 1) / 2f, 1 - ((y / radius) + 1) / 2f,   0f, 0f, 1f,   0f);
        }

        for (int i = 0; i < n; i++) {
            pushIndex(0).pushIndex(i + 1).pushIndex(i + 2);
        }
    }

    @Override
    public Mesh copy() {
        CircleMesh mesh = new CircleMesh(radius, sides);
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
