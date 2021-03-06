package xyz.destiall.caramel.app.editor.debug;

import caramel.api.components.Camera;
import caramel.api.interfaces.Render;
import caramel.api.interfaces.Update;
import caramel.api.render.BatchRenderer;
import caramel.api.render.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
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
    private float[] vertexArray = new float[500 * 6 * 2];

    private int vaoID;
    private int vboID;
    private Shader shader;
    private boolean started = false;

    private void init() {
        shader = Shader.getShader("line");
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long) vertexArray.length * Float.BYTES, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glLineWidth(5.f);
    }

    public void addLine(Vector3f from, Vector3f to, Vector3f color) {
        DebugLine line = new DebugLine(from, to, color, 1);
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
        vertexArray = new float[lines.size() * 2 * 6];

        int index = 0;
        for (DebugLine line : lines) {
            for (int i = 0; i < 2; i++) {
                Vector3f pos = i == 0 ? line.from : line.to;
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
    public void render(Camera camera) {
        if (lines.isEmpty()) return;

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray);

        shader.attach();
        Matrix4f vp = camera.getProjection().mul(camera.getView());
        shader.uploadMat4f("uVP", vp);

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawArrays(GL_LINES, 0, lines.size() * 2);
        BatchRenderer.DRAW_CALLS++;

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        shader.detach();
    }
}
