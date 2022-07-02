package xyz.destiall.caramel.app.editor.debug;

import org.joml.Vector2f;
import org.joml.Vector3f;

public final class DebugLine {
    public Vector3f from;
    public Vector3f to;
    public Vector3f color;
    public int lifetime;

    public DebugLine(Vector3f from, Vector3f to, Vector3f color, int lifetime) {
        this.from = from;
        this.to = to;
        this.color = color;
        this.lifetime = lifetime;
    }

    public DebugLine(Vector2f from, Vector2f to, Vector3f color, int lifetime) {
        this.from = new Vector3f(from.x, from.y, 0);
        this.to = new Vector3f(to.x, to.y, 0);
        this.color = color;
        this.lifetime = lifetime;
    }

    public int beginFrame() {
        lifetime--;
        return lifetime;
    }
}
