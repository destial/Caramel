package xyz.destiall.caramel.api.debug;

import org.joml.Matrix3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.app.editor.debug.DebugDraw;
import xyz.destiall.caramel.app.editor.panels.ConsolePanel;

public final class DebugImpl extends Debug {
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
        drawLine(
                new Vector3f(center.x - offset.x * 0.5f, center.y + offset.y * 0.5f, center.z),
                new Vector3f(center.x + offset.x * 0.5f, center.y + offset.y * 0.5f, center.z),
                color
        );
        drawLine(
                new Vector3f(center.x - offset.x * 0.5f, center.y - offset.y * 0.5f, center.z),
                new Vector3f(center.x - offset.x * 0.5f, center.y + offset.y * 0.5f, center.z),
                color
        );
        drawLine(
                new Vector3f(center.x - offset.x * 0.5f, center.y - offset.y * 0.5f, center.z),
                new Vector3f(center.x + offset.x * 0.5f, center.y - offset.y * 0.5f, center.z),
                color
        );
        drawLine(
                new Vector3f(center.x + offset.x * 0.5f, center.y - offset.y * 0.5f, center.z),
                new Vector3f(center.x + offset.x * 0.5f, center.y + offset.y * 0.5f, center.z),
                color
        );
    }

    public static void drawOutline(Transform transform, Vector3f color) {
        float x = transform.position.x + transform.localPosition.x;
        float y = transform.position.y + transform.localPosition.y;
        float z = transform.position.z + transform.localPosition.z;
        drawLine(
                new Vector3f(-0.5f, 0.5f, -z).mul(transform.model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(0.5f, 0.5f, -z).mul(transform.model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
        drawLine(
                new Vector3f(-0.5f, -0.5f, -z).mul(transform.model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(-0.5f, 0.5f, -z).mul(transform.model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
        drawLine(
                new Vector3f(-0.5f, -0.5f, -z).mul(transform.model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(0.5f, -0.5f, -z).mul(transform.model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
        drawLine(
                new Vector3f(0.5f, -0.5f, -z).mul(transform.model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(0.5f, 0.5f, -z).mul(transform.model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
    }

    public static void drawOutline(Transform transform, Vector3f offset, Vector3f color) {
        float x = transform.position.x + transform.localPosition.x;
        float y = transform.position.y + transform.localPosition.y;
        float z = transform.position.z + transform.localPosition.z;
        Matrix4f model = new Matrix4f().identity();
        model
        .identity()
        .translate(transform.position).translate(transform.localPosition)
        .rotate(transform.rotation.x, 1, 0, 0)
        .rotate(transform.rotation.y, 0, 1, 0)
        .rotate(transform.rotation.z, 0, 0, 1)
        .scale(offset);
        drawLine(
                new Vector3f(-0.5f, 0.5f, -z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(0.5f, 0.5f, -z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
        drawLine(
                new Vector3f(-0.5f, -0.5f, -z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(-0.5f, 0.5f, -z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
        drawLine(
                new Vector3f(-0.5f, -0.5f, -z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(0.5f, -0.5f, -z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
        drawLine(
                new Vector3f(0.5f, -0.5f, -z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(0.5f, 0.5f, -z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
    }

    public static void drawBox3D(Vector3f from, Vector3f to, Vector3f color) {
        drawLine(from, new Vector3f(from.x, to.y, from.z), color);
        drawLine(from, new Vector3f(from.x, from.y, to.z), color);
        drawLine(from, new Vector3f(to.x, from.y, from.z), color);
        drawLine(from, new Vector3f(to.x, from.y, from.z), color);

        drawLine(to, new Vector3f(to.x, from.y, to.z), color);
        drawLine(to, new Vector3f(to.x, to.y, from.z), color);
        drawLine(to, new Vector3f(to.x, from.y, from.z), color);
        drawLine(to, new Vector3f(from.x, to.y, from.z), color);

        drawLine(new Vector3f(to.x, from.y, from.z), new Vector3f(from.x, to.y, to.z), color);
    }

    @Override
    protected void _log(Object log) {
        log(log);
    }

    @Override
    protected void _logError(Object error) {
        logError(error);
    }

    @Override
    protected void _drawLine(Vector3f from, Vector3f offset, Vector3f color) {
        drawLine(from, offset, color);
    }

    @Override
    protected void _drawBox2D(Vector3f center, Vector3f offset, Vector3f color) {
        drawBox2D(center, offset, color);
    }

    @Override
    protected void _drawBox3D(Vector3f center, Vector3f offset, Vector3f color) {
        drawBox3D(center, offset, color);
    }

    @Override
    protected void _drawOutline(Transform transform, Vector3f color) {
        drawOutline(transform, color);
    }

    @Override
    protected void _drawOutline(Transform transform, Vector3f offset, Vector3f color) {
        drawOutline(transform, offset, color);
    }
}
