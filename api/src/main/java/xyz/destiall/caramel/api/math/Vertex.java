package xyz.destiall.caramel.api.math;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Vertex {
    public Vector3f position;
    public Vector2f texCoords;
    public Vector4f color;
    public Vector3f normal;

    public Vertex() {
        position = new Vector3f();
        texCoords = new Vector2f();
        color = new Vector4f();
        normal = new Vector3f();
    }

    public static final int SIZE = 3 + 2 + 4 + 3;
}
