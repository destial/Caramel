package xyz.destiall.caramel.app.editor.debug;

import caramel.api.components.Camera;
import caramel.api.graphics.Graphics;
import caramel.api.interfaces.Render;
import caramel.api.interfaces.Update;
import caramel.api.render.BatchRenderer;
import caramel.api.render.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public final class DebugDraw implements Update, Render {
    public static DebugDraw INSTANCE = new DebugDraw();

    private final List<DebugLine> lines = new ArrayList<>();
    private int MAX_SIZE = 500 * 6 * 2;
    private float[] vertexArray = new float[MAX_SIZE];

    private int vaoID;
    private int vboID;
    private Shader shader;
    private boolean started = false;

    private void init() {
        shader = Shader.getShader("line");
        vaoID = Graphics.get().glGenVertexArrays();
        Graphics.get().glBindVertexArray(vaoID);

        vboID = Graphics.get().glGenBuffers();
        Graphics.get().glBindBuffer(GL_ARRAY_BUFFER, vboID);
        Graphics.get().glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_DYNAMIC_DRAW);

        Graphics.get().glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        Graphics.get().glEnableVertexAttribArray(0);

        Graphics.get().glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        Graphics.get().glEnableVertexAttribArray(1);

        Graphics.get().glLineWidth(5.f);
    }

    public void addLine(final Vector3f from, final Vector3f to, final Vector3f color) {
        final DebugLine line = new DebugLine(from, to, color, 1);
        lines.add(line);
    }

    @Override
    public void update() {
        if (!started) {
            init();
            started = true;
        }

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).beginFrame() < 0) {
                lines.remove(i);
                i--;
            }
        }

        if (lines.isEmpty()) return;
        final int size = lines.size() * 2 * 6;
        vertexArray = new float[size];

        int index = 0;
        for (final DebugLine line : lines) {
            for (int i = 0; i < 2; i++) {
                final Vector3f pos = i == 0 ? line.from : line.to;
                vertexArray[  index  ] = pos.x;
                vertexArray[index + 1] = pos.y;
                vertexArray[index + 2] = pos.z;

                vertexArray[index + 3] = line.color.x;
                vertexArray[index + 4] = line.color.y;
                vertexArray[index + 5] = line.color.z;
                index += 6;
            }
        }
    }

    @Override
    public void render(final Camera camera) {
        if (lines.isEmpty()) return;

        Graphics.get().glBindBuffer(GL_ARRAY_BUFFER, vboID);
        if (vertexArray.length > MAX_SIZE) {
            MAX_SIZE = vertexArray.length;
            Graphics.get().glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_DYNAMIC_DRAW);
        } else {
            Graphics.get().glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray);
        }

        shader.attach();
        final Matrix4f vp = camera.getProjection().mul(camera.getView());
        shader.uploadMat4f("uVP", vp);

        Graphics.get().glBindVertexArray(vaoID);
        Graphics.get().glEnableVertexAttribArray(0);
        Graphics.get().glEnableVertexAttribArray(1);

        Graphics.get().glDrawArrays(GL_LINES, 0, lines.size() * 2);
        BatchRenderer.DRAW_CALLS++;

        Graphics.get().glDisableVertexAttribArray(0);
        Graphics.get().glDisableVertexAttribArray(1);
        Graphics.get().glBindVertexArray(0);

        Graphics.get().glBindBuffer(GL_ARRAY_BUFFER, 0);

        shader.detach();
    }
}
