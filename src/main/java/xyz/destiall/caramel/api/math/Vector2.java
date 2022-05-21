package xyz.destiall.caramel.api.math;

import imgui.ImVec2;
import org.jbox2d.common.Vec2;
import org.joml.Vector2f;

public class Vector2 {
    private final Vector2f joml;
    private final Vec2 jbox2d;
    private final ImVec2 imgui;

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
        return joml.x;
    }

    public float y() {
        return joml.y();
    }

    public ImVec2 getImgui() {
        return imgui;
    }

    public Vector2f getJoml() {
        return joml;
    }

    public Vec2 getJbox2d() {
        return jbox2d;
    }
}
