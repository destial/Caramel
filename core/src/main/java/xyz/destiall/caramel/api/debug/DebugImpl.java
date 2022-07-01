package xyz.destiall.caramel.api.debug;

import org.joml.Vector3f;
import xyz.destiall.caramel.app.editor.debug.DebugDraw;
import xyz.destiall.caramel.app.editor.ui.ConsolePanel;

public class DebugImpl extends Debug {
    static {
        inst = new DebugImpl();
    }

    public static void log(Object log) {
        System.out.println(log);
        ConsolePanel.addLog(""+log);
    }

    public static void logError(Object error) {
        System.out.println(error);
        ConsolePanel.addError("ERROR: " + error);
    }

    public static void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        DebugDraw.INSTANCE.addLine(from, to, color);
    }

    public static void drawBox2D(Vector3f center, Vector3f offset, Vector3f color) {
        DebugDraw.INSTANCE.addLine(
                new Vector3f(center.x - offset.x * 0.5f, center.y + offset.y * 0.5f, center.z),
                new Vector3f(center.x + offset.x * 0.5f, center.y + offset.y * 0.5f, center.z),
                color
        );
        DebugDraw.INSTANCE.addLine(
                new Vector3f(center.x - offset.x * 0.5f, center.y - offset.y * 0.5f, center.z),
                new Vector3f(center.x - offset.x * 0.5f, center.y + offset.y * 0.5f, center.z),
                color
        );
        DebugDraw.INSTANCE.addLine(
                new Vector3f(center.x - offset.x * 0.5f, center.y - offset.y * 0.5f, center.z),
                new Vector3f(center.x + offset.x * 0.5f, center.y - offset.y * 0.5f, center.z),
                color
        );
        DebugDraw.INSTANCE.addLine(
                new Vector3f(center.x + offset.x * 0.5f, center.y - offset.y * 0.5f, center.z),
                new Vector3f(center.x + offset.x * 0.5f, center.y + offset.y * 0.5f, center.z),
                color
        );
    }

    public static void drawBox3D(Vector3f from, Vector3f to, Vector3f color) {
        DebugDraw.INSTANCE.addLine(from, new Vector3f(from.x, to.y, from.z), color);
        DebugDraw.INSTANCE.addLine(from, new Vector3f(from.x, from.y, to.z), color);
        DebugDraw.INSTANCE.addLine(from, new Vector3f(to.x, from.y, from.z), color);
        DebugDraw.INSTANCE.addLine(from, new Vector3f(to.x, from.y, from.z), color);

        DebugDraw.INSTANCE.addLine(to, new Vector3f(to.x, from.y, to.z), color);
        DebugDraw.INSTANCE.addLine(to, new Vector3f(to.x, to.y, from.z), color);
        DebugDraw.INSTANCE.addLine(to, new Vector3f(to.x, from.y, from.z), color);
        DebugDraw.INSTANCE.addLine(to, new Vector3f(from.x, to.y, from.z), color);

        DebugDraw.INSTANCE.addLine(new Vector3f(to.x, from.y, from.z), new Vector3f(from.x, to.y, to.z), color);
    }
}
