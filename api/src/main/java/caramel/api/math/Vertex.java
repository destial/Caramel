package caramel.api.math;

import caramel.api.utils.Color;
import org.joml.Vector2f;
import org.joml.Vector3f;

public final class Vertex {
    public Vector3f position;
    public Vector2f texCoords;
    public Color color;
    public Vector3f normal;

    public Vertex() {
        position = new Vector3f();
        texCoords = new Vector2f();
        color = new Color();
        normal = new Vector3f();
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "position=" + position +
                ", texCoords=" + texCoords +
                ", color=" + color +
                ", normal=" + normal +
                '}';
    }

    public static final int SIZE = 3 + 2 + 4 + 3;
}
