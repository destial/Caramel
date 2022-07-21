package caramel.api.utils;

import org.joml.Vector4f;

public final class Color {
    private final Vector4f vector;
    public float r = 1f, g = 1f, b = 1f, a = 1f;

    public Color() {
        vector = new Vector4f(r, g, b, a);
    }

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        vector = new Vector4f(r, g, b, a);
    }

    public void set(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        vector.set(r, g, b, a);
    }

    public Vector4f asVector() {
        return vector.set(r, g, b, a);
    }
}
