package caramel.api.text;

import caramel.api.components.Camera;
import caramel.api.components.Transform;
import caramel.api.render.BatchRenderer;
import caramel.api.render.Shader;
import caramel.api.render.Text;
import caramel.api.utils.Color;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_TEXTURE0;
import static org.lwjgl.opengl.GL15.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.glActiveTexture;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBindTexture;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glDrawElements;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

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

    public void setFont(TextFont font) {
        this.font = font;
    }

    public TextMesh() {
        shader = Shader.getShader("text");
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * VERTEX_SIZE * BATCH_SIZE, GL_DYNAMIC_DRAW);

        int elementSize = BATCH_SIZE * 3;
        int[] elementBuffer = new int[elementSize];
        int[] indices = {
                0, 1, 3,
                1, 2, 3
        };

        for (int i=0; i < elementSize; i++) {
            elementBuffer[i] = indices[(i % 6)] + ((i / 6) * 4);
        }

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        int stride = VERTEX_SIZE * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 5 * Float.BYTES);
        glEnableVertexAttribArray(2);

        Text.addMesh(this);
    }

    public void invalidate() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        if (ebo != 0) {
            glDeleteBuffers(ebo);
        }
    }

    public void render(Transform transform, Camera camera) {
        shader.attach();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, font.texture.getTexId());
        shader.uploadTexture("texSampler", 0);

        Matrix4f mvp = camera.getProjection().mul(camera.getView()).mul(transform.getModel());
        shader.uploadMat4f("uMVP", mvp);

        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glDrawElements(GL_TRIANGLES, size * 6, GL_UNSIGNED_INT, 0);
        BatchRenderer.DRAW_CALLS++;

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);

        for (int i = 0; i < size * 4 * VERTEX_SIZE; i++) {
            vertices[i] = 0;
        }
        size = 0;

        glBindTexture(GL_TEXTURE_2D, 0);
        shader.detach();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean addCharacter(float x, float y, CharInfo charInfo) {
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

        float x1 = x + charInfo.width;
        float y1 = y + charInfo.height;

        float ux0 = charInfo.textureCoordinates[0].x;
        float uy0 = charInfo.textureCoordinates[0].y;
        float ux1 = charInfo.textureCoordinates[1].x;
        float uy1 = charInfo.textureCoordinates[1].y;

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

    public void addText(String text) {
        float length = 0;
        float height = 0;
        for (int i=0; i < text.length(); i++) {
            char c = text.charAt(i);
            CharInfo charInfo = font.getCharacter(c);
            if (charInfo.width == 0) {
                System.out.println("Unknown character " + c);
                continue;
            }
            height = Math.max(height, charInfo.height);
            length += charInfo.width;
        }

        float x = -length / 2;
        float y = -height / 4;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            CharInfo charInfo = font.getCharacter(c);
            if (charInfo.width == 0) {
                System.out.println("Unknown character " + c);
                continue;
            }
            float xPos = x;
            addCharacter(xPos, y, charInfo);
            x += charInfo.width;

        }
    }
}
