package caramel.api.utils;

import caramel.api.interfaces.Copyable;
import org.joml.Math;

public final class Color implements Copyable<Color> {
    public float r = 1f, g = 1f, b = 1f, a = 1f;

    public Color() {}

    public Color(final Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    public Color(final float r, final float g, final float b, final float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void set(final float r, final float g, final float b, final float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void set(final Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    public static void lerp(final Color target, final Color from, final float delta) {
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

    @Override
    public Color copy() {
        return new Color(r, g, b, a);
    }
}
