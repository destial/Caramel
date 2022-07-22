package caramel.api.utils;

import org.joml.Math;

public final class Color {
    public float r = 1f, g = 1f, b = 1f, a = 1f;

    public Color() {}

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void set(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void set(Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    public static void lerp(Color target, Color from, float delta) {
        from.r = Math.fma(target.r - from.r, delta, from.r);
        from.g = Math.fma(target.g - from.g, delta, from.g);
        from.b = Math.fma(target.b - from.b, delta, from.b);
        from.a = Math.fma(target.a - from.a, delta, from.a);
    }

    @Override
    public String toString() {
        return "Color{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", a=" + a +
                '}';
    }
}
