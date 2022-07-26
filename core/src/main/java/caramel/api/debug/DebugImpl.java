package caramel.api.debug;

import caramel.api.components.Transform;
import org.joml.Matrix3d;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.editor.debug.DebugDraw;
import xyz.destiall.caramel.app.editor.debug.DebugLine;
import xyz.destiall.caramel.app.editor.panels.ConsolePanel;

public final class DebugImpl extends Debug {
    static {
        inst = new DebugImpl();
    }

    public static void log(Object log) {
        console(log);
        ConsolePanel.addLog(""+log);
    }

    public static void logError(Object error) {
        System.err.println(error);
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

        Matrix4f model = transform.getModel();
        drawLine(
                new Vector3f(-0.5f, 0.5f, z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(0.5f, 0.5f, z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
        drawLine(
                new Vector3f(-0.5f, -0.5f, z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(-0.5f, 0.5f, z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
        drawLine(
                new Vector3f(-0.5f, -0.5f, z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(0.5f, -0.5f, z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
        drawLine(
                new Vector3f(0.5f, -0.5f, z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                new Vector3f(0.5f, 0.5f, z).mul(model.get3x3(new Matrix3d())).add(x, y, 0),
                color
        );
    }

    public static void drawOutline(Transform transform, float radius, Vector3f color) {
        float x = transform.position.x + transform.localPosition.x;
        float y = transform.position.y + transform.localPosition.y;
        float z = transform.position.z + transform.localPosition.z;
        float prevX = -1;
        float prevY = -1;

        Matrix4f model = new Matrix4f().identity();
        model
                .identity()
                .rotate(transform.rotation.x, 1, 0, 0)
                .rotate(transform.rotation.y, 0, 1, 0)
                .rotate(transform.rotation.z, 0, 0, 1);

        int n = (int) (radius * 10);
        n = Math.min(n, 100);
        for (double theta = -Math.PI; theta <= Math.PI; theta += Math.PI / n) {
            double xx = Math.cos(theta) * radius;
            double yy = Math.sin(theta) * radius;
            if (prevX == -1) {
                prevX = (float) xx;
                prevY = (float) yy;
                continue;
            }
            Vector3f start = new Vector3f(prevX, prevY, -z).mul(model.get3x3(new Matrix3d())).add(x, y, 0);
            Vector3f end = new Vector3f((float) xx, (float) yy, -z).mul(model.get3x3(new Matrix3d())).add(x, y, 0);
            drawLine(start, end, color);

            prevX = (float) xx;
            prevY = (float) yy;
        }
    }

    public static void drawOutline(Transform transform, Vector3f offset, Vector3f color) {
        float x = transform.position.x + transform.localPosition.x;
        float y = transform.position.y + transform.localPosition.y;
        float z = transform.position.z + transform.localPosition.z;
        Matrix4f model = new Matrix4f().identity();
        model
        .identity()
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

    public static void drawOutline(Transform transform, Vector3f scale, Vector3f offset, Vector3f color) {
        float x = transform.position.x + transform.localPosition.x + offset.x;
        float y = transform.position.y + transform.localPosition.y + offset.y;
        float z = transform.position.z + transform.localPosition.z + offset.z;
        Matrix4f model = new Matrix4f().identity();
        model
                .identity()
                .rotate(transform.rotation.x, 1, 0, 0)
                .rotate(transform.rotation.y, 0, 1, 0)
                .rotate(transform.rotation.z, 0, 0, 1)
                .scale(scale);
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

    public static void drawDotScreen(float x, float y, Vector3f color) {
        DebugLine line = new DebugLine(new Vector2f(x, y), new Vector2f(x, y), color, 1);
        ApplicationImpl.getApp().getImGui().lines.add(line);
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
    protected void _drawOutline(Transform transform, float radius, Vector3f color) {
        drawOutline(transform, radius, color);
    }

    @Override
    protected void _drawOutline(Transform transform, Vector3f scale, Vector3f color) {
        drawOutline(transform, scale, color);
    }

    @Override
    protected void _drawOutline(Transform transform, Vector3f scale, Vector3f offset, Vector3f color) {
        drawOutline(transform, scale, offset, color);
    }

    @Override
    protected void _drawDotScreen(float x, float y, Vector3f color) {
        drawDotScreen(x, y, color);
    }
}
