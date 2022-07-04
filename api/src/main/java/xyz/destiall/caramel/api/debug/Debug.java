package xyz.destiall.caramel.api.debug;

import org.joml.Vector3f;

public abstract class Debug {
    protected static Debug inst;

    public static void log(Object log) {
        inst._log(log);
    }

    public static void logError(Object error) {
        inst._logError(error);
    }

    public static void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        inst._drawLine(from, to, color);
    }

    public static void drawBox2D(Vector3f center, Vector3f offset, Vector3f color) {
        inst._drawBox2D(center, offset, color);
    }

    public static void drawBox3D(Vector3f from, Vector3f to, Vector3f color) {
        inst._drawBox3D(from, to, color);
    }

    protected abstract void _log(Object log);
    protected abstract void _logError(Object error);
    protected abstract void _drawLine(Vector3f from, Vector3f offset, Vector3f color);
    protected abstract void _drawBox2D(Vector3f center, Vector3f offset, Vector3f color);
    protected abstract void _drawBox3D(Vector3f center, Vector3f offset, Vector3f color);
}
