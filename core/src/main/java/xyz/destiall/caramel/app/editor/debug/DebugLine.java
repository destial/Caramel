package xyz.destiall.caramel.app.editor.debug;

import org.joml.Vector2f;
import org.joml.Vector3f;

public final class DebugLine {
    public final Vector3f from;
    public final Vector3f to;
    public final Vector3f color;
    public int lifetime;

    public DebugLine(final Vector3f from, final Vector3f to, final Vector3f color, final int lifetime) {
        this.from = from;
        this.to = to;
        this.color = color;
        this.lifetime = lifetime;
    }

    public DebugLine(final Vector2f from, final Vector2f to, final Vector3f color, final int lifetime) {
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
