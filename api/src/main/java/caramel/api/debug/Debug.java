package caramel.api.debug;

import caramel.api.components.Transform;
import org.joml.Vector3f;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class Debug {
    protected static Debug inst;

    /**
     * Log a message into the console. It will automatically convert into a string.
     * @param log The message to log.
     */
    public static void log(Object log) {
        if (inst == null) return;
        inst._log(log);
    }

    /**
     * Log an error message into the console. It will automatically convert into a string.
     * @param error The error to log.
     */
    public static void logError(Object error) {
        if (inst == null) return;
        inst._logError(error);
    }

    /**
     * Draw a line in the world. This line can only be seen in the editor view.
     */
    public static void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        if (inst == null) return;
        inst._drawLine(from, to, color);
    }

    /**
     * Draw a box in the world. This box can only be seen in the editor view.
     */
    public static void drawBox2D(Vector3f center, Vector3f offset, Vector3f color) {
        if (inst == null) return;
        inst._drawBox2D(center, offset, color);
    }

    /**
     * Draw a box in the world. This box can only be seen in the editor view.
     */
    public static void drawBox3D(Vector3f from, Vector3f to, Vector3f color) {
        if (inst == null) return;
        inst._drawBox3D(from, to, color);
    }

    /**
     * Draw an outline of the {@link Transform} component.
     */
    public static void drawOutline(Transform transform, Vector3f color) {
        if (inst == null) return;
        inst._drawOutline(transform, color);
    }

    /**
     * Draw an outline of the {@link Transform} component.
     */
    public static void drawOutline(Transform transform, float radius, Vector3f color) {
        if (inst == null) return;
        inst._drawOutline(transform, radius, color);
    }

    /**
     * Draw an outline of the {@link Transform} component.
     */
    public static void drawOutline(Transform transform, Vector3f scale, Vector3f color) {
        if (inst == null) return;
        inst._drawOutline(transform, scale, color);
    }

    /**
     * Draw an outline of the {@link Transform} component.
     */
    public static void drawOutline(Transform transform, Vector3f scale, Vector3f offset, Vector3f color) {
        if (inst == null) return;
        inst._drawOutline(transform, scale, offset, color);
    }

    /**
     * Draw a dot on the screen. This uses screen coordinates instead of world coordinates.
     */
    public static void drawDotScreen(float x, float y, Vector3f color) {
        if (inst == null) return;
        inst._drawDotScreen(x, y, color);
    }

    /**
     * Formats a message including its date and time sent.
     */
    public static void console(Object message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println("[" + dateFormat.format(cal.getTime()) + "]: " + message);
    }

    protected abstract void _log(Object log);
    protected abstract void _logError(Object error);
    protected abstract void _drawLine(Vector3f from, Vector3f offset, Vector3f color);
    protected abstract void _drawBox2D(Vector3f center, Vector3f offset, Vector3f color);
    protected abstract void _drawBox3D(Vector3f center, Vector3f offset, Vector3f color);
    protected abstract void _drawOutline(Transform transform, Vector3f color);
    protected abstract void _drawOutline(Transform transform, float radius, Vector3f color);
    protected abstract void _drawOutline(Transform transform, Vector3f scale, Vector3f color);
    protected abstract void _drawOutline(Transform transform, Vector3f scale, Vector3f offset, Vector3f color);
    protected abstract void _drawDotScreen(float x, float y, Vector3f color);
}
