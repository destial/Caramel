package caramel.api.math;

import org.jbox2d.common.Vec3;
import org.joml.Vector3f;
import org.ode4j.math.DVector3;

public final class Vector3 {
    private final Vector3f joml;
    private final Vec3 jbox2d;
    private final DVector3 ode;

    public Vector3() {
        this(0);
    }

    public Vector3(float i) {
        this(i, i, i);
    }

    public Vector3(Vector3f vect) {
        this(vect.x, vect.y, vect.z);
    }

    public Vector3(DVector3 vect) {
        this((float) vect.get0(), (float) vect.get1(), (float) vect.get2());
    }

    public Vector3(Vec3 vect) {
        this(vect.x, vect.y, vect.z);
    }

    public Vector3(float x, float y, float z) {
        joml = new Vector3f(x, y, z);
        jbox2d = new Vec3(x, y, z);
        ode = new DVector3(x, y, z);
    }

    public void set(float x, float y, float z) {
        joml.set(x, y, z);
        jbox2d.set(x, y, z);
        ode.set(x, y, z);
    }

    public void set(double x, double y, double z) {
        joml.set(x, y, z);
        jbox2d.set((float) x, (float) y, (float) z);
    }

    public float x() {
        return joml.x;
    }

    public float y() {
        return joml.y;
    }

    public float z() {
        return joml.z;
    }

    public DVector3 getOde() {
        ode.set(x(), y(), z());
        return ode;
    }

    public Vector3f getJoml() {
        return joml;
    }

    public Vec3 getJbox2d() {
        jbox2d.set(x(), y(), z());
        return jbox2d;
    }

    public static Vector3 lerp(Vector3 vector, Vector3 target, float dt) {
        vector.joml.lerp(target.joml, dt);
        return vector;
    }

    @Override
    public String toString() {
        return joml.toString();
    }
}
