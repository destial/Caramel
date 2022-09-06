package caramel.api.text;

import caramel.api.components.Camera;
import caramel.api.components.Transform;
import caramel.api.debug.Debug;
import caramel.api.graphics.Graphics;
import caramel.api.render.BatchRenderer;
import caramel.api.render.Shader;
import caramel.api.render.Text;
import caramel.api.utils.Color;
import org.joml.Matrix4f;

import static caramel.api.graphics.GL20.GL_ARRAY_BUFFER;
import static caramel.api.graphics.GL20.GL_DYNAMIC_DRAW;
import static caramel.api.graphics.GL20.GL_ELEMENT_ARRAY_BUFFER;
import static caramel.api.graphics.GL20.GL_FLOAT;
import static caramel.api.graphics.GL20.GL_STATIC_DRAW;
import static caramel.api.graphics.GL20.GL_TEXTURE0;
import static caramel.api.graphics.GL20.GL_TEXTURE_2D;
import static caramel.api.graphics.GL20.GL_TRIANGLES;
import static caramel.api.graphics.GL20.GL_UNSIGNED_INT;

public final class TextMesh {
    private static final int BATCH_SIZE = 1000;
    private static final int VERTEX_SIZE = 7;
    private final float[] vertices = new float[BATCH_SIZE * VERTEX_SIZE];
    private transient Color color;
    private transient final int vao;
    private transient final int vbo;
    private transient final int ebo;
    private transient final Shader shader;

    private int size = 0;
    private TextFont font;

    public void setFont(final TextFont font) {
        this.font = font;
    }

    public TextMesh() {
        shader = Shader.getShader("text");
        vao = Graphics.get().glGenVertexArrays();
        Graphics.get().glBindVertexArray(vao);

        vbo = Graphics.get().glGenBuffers();
        Graphics.get().glBindBuffer(GL_ARRAY_BUFFER, vbo);
        Graphics.get().glBufferData(GL_ARRAY_BUFFER, Float.BYTES * VERTEX_SIZE * BATCH_SIZE, GL_DYNAMIC_DRAW);

        final int elementSize = BATCH_SIZE * 3;
        final int[] elementBuffer = new int[elementSize];
        final int[] indices = {
                0, 1, 3,
                1, 2, 3
        };

        for (int i = 0; i < elementSize; i++) {
            elementBuffer[i] = indices[(i % 6)] + ((i / 6) * 4);
        }

        ebo = Graphics.get().glGenBuffers();
        Graphics.get().glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        Graphics.get().glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        final int stride = VERTEX_SIZE * Float.BYTES;
        Graphics.get().glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        Graphics.get().glEnableVertexAttribArray(0);

        Graphics.get().glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 2 * Float.BYTES);
        Graphics.get().glEnableVertexAttribArray(1);

        Graphics.get().glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 5 * Float.BYTES);
        Graphics.get().glEnableVertexAttribArray(2);

        Text.addMesh(this);
    }

    public void invalidate() {
        Graphics.get().glDeleteVertexArrays(vao);
        Graphics.get().glDeleteBuffers(vbo);
        if (ebo != 0) {
            Graphics.get().glDeleteBuffers(ebo);
        }
    }

    public void render(final Transform transform, final Camera camera) {
        shader.attach();
        Graphics.get().glBindBuffer(GL_ARRAY_BUFFER, vbo);
        Graphics.get().glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        Graphics.get().glActiveTexture(GL_TEXTURE0);
        Graphics.get().glBindTexture(GL_TEXTURE_2D, font.texture.getTexId());
        shader.uploadTexture("texSampler", 0);

        final Matrix4f mvp = camera.getProjection().mul(camera.getView()).mul(transform.getModel());
        shader.uploadMat4f("uMVP", mvp);

        Graphics.get().glBindVertexArray(vao);
        Graphics.get().glEnableVertexAttribArray(0);
        Graphics.get().glEnableVertexAttribArray(1);
        Graphics.get().glEnableVertexAttribArray(2);

        Graphics.get().glDrawElements(GL_TRIANGLES, size * 6, GL_UNSIGNED_INT, 0);
        BatchRenderer.DRAW_CALLS++;

        Graphics.get().glDisableVertexAttribArray(0);
        Graphics.get().glDisableVertexAttribArray(1);
        Graphics.get().glDisableVertexAttribArray(2);
        Graphics.get().glBindVertexArray(0);

        for (int i = 0; i < size * 4 * VERTEX_SIZE; i++) {
            vertices[i] = 0;
        }
        size = 0;

        Graphics.get().glBindTexture(GL_TEXTURE_2D, 0);
        shader.detach();
    }

    public void setColor(final Color color) {
        this.color = color;
    }

    public boolean addCharacter(final float x, final float y, final CharInfo charInfo) {
        // If we have no more room in the current batch, flush it and start with a fresh batch
        if (size >= BATCH_SIZE - 4) {
            return false;
        }

        float r = 1f;
        float g = 1f;
        float b = 1f;

        if (color != null) {
            r = color.r;
            g = color.g;
            b = color.b;
        }

        final float x1 = x + charInfo.width;
        final float y1 = y + charInfo.height;

        final float ux0 = charInfo.textureCoordinates[0].x;
        final float uy0 = charInfo.textureCoordinates[0].y;
        final float ux1 = charInfo.textureCoordinates[1].x;
        final float uy1 = charInfo.textureCoordinates[1].y;

        int index = size * VERTEX_SIZE;
        vertices[index] = x1;      vertices[index + 1] = y;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux1; vertices[index + 6] = uy0;

        index += VERTEX_SIZE;
        vertices[index] = x1;      vertices[index + 1] = y1;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux1; vertices[index + 6] = uy1;

        index += VERTEX_SIZE;
        vertices[index] = x;      vertices[index + 1] = y1;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux0; vertices[index + 6] = uy1;

        index += VERTEX_SIZE;
        vertices[index] = x;      vertices[index + 1] = y;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux0; vertices[index + 6] = uy0;

        size += 4;
        return true;
    }

    public void addText(final String text) {
        float length = 0;
        float height = 0;
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            final CharInfo charInfo = font.getCharacter(c);
            if (charInfo.width == 0) {
                Debug.console("Unknown character " + c);
                continue;
            }
            height = Math.max(height, charInfo.height);
            length += charInfo.width;
        }

        float x = -length / 2;
        float y = -height / 4;
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            final CharInfo charInfo = font.getCharacter(c);
            if (charInfo.width == 0) {
                Debug.console("Unknown character " + c);
                continue;
            }
            final float xPos = x;
            addCharacter(xPos, y, charInfo);
            x += charInfo.width;

        }
    }
}
