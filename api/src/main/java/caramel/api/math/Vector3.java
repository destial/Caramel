package caramel.api.math;

import caramel.api.interfaces.Copyable;
import org.jbox2d.common.Vec3;
import org.joml.Vector3f;
import org.ode4j.math.DVector3;

/**
 * Math wrapper for the API to convert into each specific library.
 */
public final class Vector3 implements Copyable<Vector3> {
    private final Vector3f joml;
    private final Vec3 jbox2d;
    private final DVector3 ode;

    public Vector3() {
        this(0);
    }

    public Vector3(final float i) {
        this(i, i, i);
    }

    public Vector3(final Vector3f vect) {
        this(vect.x, vect.y, vect.z);
    }

    public Vector3(final DVector3 vect) {
        this((float) vect.get0(), (float) vect.get1(), (float) vect.get2());
    }

    public Vector3(final Vector3 value) {
        this(value == null ? 0 : value.x(), value == null ? 0 : value.y(), value == null ? 0 : value.z());
    }

    public Vector3(final Vec3 vect) {
        this(vect.x, vect.y, vect.z);
    }

    public Vector3(final float x, final float y, final float z) {
        joml = new Vector3f(x, y, z);
        jbox2d = new Vec3(x, y, z);
        ode = new DVector3(x, y, z);
    }

    public void set(final float x, final float y, final float z) {
        joml.set(x, y, z);
        jbox2d.set(x, y, z);
        ode.set(x, y, z);
    }

    public void set(final double x, final double y, final double z) {
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

    public static Vector3 lerp(final Vector3 vector, final Vector3 target, final float dt) {
        vector.joml.lerp(target.joml, dt);
        return vector;
    }

    @Override
    public String toString() {
        return joml.toString();
    }

    public void set(final Vector3 value) {
        set(value.x(), value.y(), value.z());
    }

    @Override
    public Vector3 copy() {
        return new Vector3(x(), y(), z());
    }
}
