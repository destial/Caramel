package xyz.destiall.caramel.api.debug;

import org.joml.Vector3f;

public abstract class Debug {
    protected static Debug inst;

    public static void log(Object log) {
        inst.log(log);
    }

    public static void logError(Object error) {
        inst.logError(error);
    }

    public static void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        inst.drawLine(from, to, color);
    }

    public static void drawBox2D(Vector3f center, Vector3f offset, Vector3f color) {
        inst.drawBox2D(center, offset, color);
    }

    public static void drawBox3D(Vector3f from, Vector3f to, Vector3f color) {
        inst.drawBox3D(from, to, color);
    }
}
