package caramel.api.texture;

import static org.jbox2d.common.MathUtils.TWOPI;
import static org.lwjgl.opengl.GL11.GL_LINES;

public final class MeshBuilder {

    public static Mesh createCircle(float radius, int n) {
        Mesh mesh = new Mesh();
        mesh.pushVertex(0f, 0f, 0f,   1f, 1f, 1f, 1f,   0.5f, 0.5f,   0f, 0f, 1f,   0f);

        for (int i = 0; i <= n; i++) {
            float x = (float) (radius * Math.sin(TWOPI * i / n));
            float y = (float) (radius * Math.cos(TWOPI * i / n));
            mesh.pushVertex(x, y, 0f,   1f, 1f, 1f, 1f,   ((x / radius) + 1) / 2f, 1 - ((y / radius) + 1) / 2f,   0f, 0f, 1f,   0f);
        }

        for (int i = 0; i < n; i++) {
            mesh.pushIndex(0).pushIndex(i + 1).pushIndex(i + 2);
        }
        return mesh;
    }

    public static Mesh createQuad(float length) {
        Mesh mesh = new Mesh();
        mesh.pushVertex( 0.5f * length, -0.5f * length, 0f,   1f, 1f, 1f, 1f,   1f, 1f,   0f, 0f, 0f,   0f)
            .pushVertex(-0.5f * length,  0.5f * length, 0f,   1f, 1f, 1f, 1f,   0f, 0f,   0f, 0f, 0f,   0f)
            .pushVertex( 0.5f * length,  0.5f * length, 0f,   1f, 1f, 1f, 1f,   1f, 0f,   0f, 0f, 0f,   0f)
            .pushVertex(-0.5f * length, -0.5f * length, 0f,   1f, 1f, 1f, 1f,   0f, 1f,   0f, 0f, 0f,   0f);

        mesh.pushIndex(2, 1, 0, 0, 1, 3);
        return mesh;
    }

    public static Mesh createAxes(float length) {
        Mesh mesh = new Mesh();
        mesh.type = GL_LINES;
        mesh.pushVertex(-length, 0, 0,  1, 0, 0, 1, 0, 0, 0, 0, 0,   0f);
        mesh.pushVertex( length, 0, 0,  1, 0, 0, 1, 0, 0, 0, 0, 0,   0f);

        mesh.pushVertex(0, -length, 0,  0, 1, 0, 1, 0, 0, 0, 0, 0,   0f);
        mesh.pushVertex(0,  length, 0,  0, 1, 0, 1, 0, 0, 0, 0, 0,   0f);

        mesh.pushVertex(0, 0, -length,  0, 0, 1, 1, 0, 0, 0, 0, 0,   0f);
        mesh.pushVertex(0, 0,  length,  0, 0, 1, 1, 0, 0, 0, 0, 0,   0f);

        mesh.pushIndex(0, 1, 2, 3, 4, 5);

        return mesh;
    }
}
