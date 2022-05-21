package xyz.destiall.caramel.api.math;

import org.jbox2d.common.Vec3;
import org.joml.Vector3f;

public class Vector3 {
    private final Vector3f joml;
    private final Vec3 jbox2d;

    public Vector3(float x, float y, float z) {
        joml = new Vector3f(x, y, z);
        jbox2d = new Vec3(x, y, z);
    }

    public void set(float x, float y, float z) {
        joml.set(x, y, z);
        jbox2d.set(x, y, z);
    }

    public float x() {
        return joml.x;
    }

    public float y() {
        return joml.y;
    }

    public float z() {
        return joml.z();
    }

    public Vector3f getJoml() {
        return joml;
    }

    public Vec3 getJbox2d() {
        return jbox2d;
    }
}
