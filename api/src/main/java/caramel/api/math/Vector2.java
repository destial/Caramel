package caramel.api.math;

import caramel.api.interfaces.Copyable;
import imgui.ImVec2;
import org.jbox2d.common.Vec2;
import org.joml.Vector2f;

/**
 * Math wrapper for the API to convert into each specific library.
 */
public final class Vector2 implements Copyable<Vector2> {
    private final Vector2f joml;
    private final Vec2 jbox2d;
    private final ImVec2 imgui;

    public Vector2() {
        this(0);
    }

    public Vector2(final Vector2f vect) {
        this(vect.x, vect.y);
    }

    public Vector2(final Vec2 vect) {
        this(vect.x, vect.y);
    }

    public Vector2(final ImVec2 vect) {
        this(vect.x, vect.y);
    }

    public Vector2(final float i) {
        this(i, i);
    }

    public Vector2(final float x, final float y) {
        joml = new Vector2f(x, y);
        jbox2d = new Vec2(x, y);
        imgui = new ImVec2(x, y);
    }

    public Vector2(final Vector2 value) {
        this(value == null ? 0f : value.x(), value == null ? 0f : value.y());
    }

    public void set(final float x, final float y) {
        joml.set(x, y);
        jbox2d.set(x, y);
        imgui.set(x, y);
    }

    public float x() {
        return joml.x();
    }

    public float distanceSquared(final Vector2 other) {
        return joml.distanceSquared(other.joml);
    }

    public float y() {
        return joml.y();
    }

    public ImVec2 getImgui() {
        imgui.set(joml.x, joml.y);
        return imgui;
    }

    public Vector2f getJoml() {
        return joml;
    }

    public Vec2 getJbox2d() {
        return jbox2d;
    }

    public static Vector2 lerp(final Vector2 vector, final Vector2 target, final float dt) {
        vector.joml.lerp(target.joml, dt);
        return vector;
    }

    @Override
    public String toString() {
        return joml.toString();
    }

    public void set(final Vector2 value) {
        set(value.x(), value.y());
    }

    @Override
    public Vector2 copy() {
        return new Vector2(x(), y());
    }
}
