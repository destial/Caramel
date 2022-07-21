package caramel.api.math;

import imgui.ImVec2;
import org.jbox2d.common.Vec2;
import org.joml.Vector2f;

public final class Vector2 {
    private final Vector2f joml;
    private final Vec2 jbox2d;
    private final ImVec2 imgui;

    public Vector2() {
        this(0);
    }

    public Vector2(Vector2f vect) {
        this(vect.x, vect.y);
    }

    public Vector2(Vec2 vect) {
        this(vect.x, vect.y);
    }

    public Vector2(ImVec2 vect) {
        this(vect.x, vect.y);
    }

    public Vector2(float i) {
        this(i, i);
    }

    public Vector2(float x, float y) {
        joml = new Vector2f(x, y);
        jbox2d = new Vec2(x, y);
        imgui = new ImVec2(x, y);
    }

    public void set(float x, float y) {
        joml.set(x, y);
        jbox2d.set(x, y);
        imgui.set(x, y);
    }

    public float x() {
        return joml.y();
    }

    public float distanceSquared(Vector2 other) {
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

    public static Vector2 lerp(Vector2 vector, Vector2 target, float dt) {
        vector.joml.lerp(target.joml, dt);
        return vector;
    }

    @Override
    public String toString() {
        return joml.toString();
    }
}
