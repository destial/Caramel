package xyz.destiall.caramel.api.text;

import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.objects.Scene;
import xyz.destiall.caramel.api.render.Shader;

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
import static org.lwjgl.opengl.GL15.glDrawElements;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_BUFFER;

public final class TextBatch {
    private final Scene scene;

    public static int BATCH_SIZE = 100;
    public static int VERTEX_SIZE = 7;
    public float[] vertices = new float[BATCH_SIZE * VERTEX_SIZE];
    public int size = 0;

    public int vao;
    public int vbo;
    public Shader shader;
    public TextFont font;

    public TextBatch(Scene scene) {
        this.scene = scene;
        shader = Shader.getShader("text");
    }

    public void render(Transform transform, Camera camera) {
        shader.use();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * VERTEX_SIZE * BATCH_SIZE, GL_DYNAMIC_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_BUFFER, font.textureId);
        shader.uploadTexture("texSampler", 0);
        shader.uploadMat4f("uProjection", camera.getProjection());
        shader.uploadMat4f("uView", camera.getView());
        shader.uploadMat4f("uModel", transform.model);

        glBindVertexArray(vao);

        glDrawElements(GL_TRIANGLES, size * 6, GL_UNSIGNED_INT, 0);

        size = 0;
        shader.detach();
    }

    public boolean addCharacter(float x, float y, CharInfo charInfo) {
        // If we have no more room in the current batch, flush it and start with a fresh batch
        if (size >= BATCH_SIZE - 4) {
            return false;
        }

        float r = 1f;
        float g = 1f;
        float b = 1f;

        float x1 = x + charInfo.width;
        float y1 = y + charInfo.height;

        float ux0 = charInfo.textureCoordinates[0].x;
        float uy0 = charInfo.textureCoordinates[0].y;
        float ux1 = charInfo.textureCoordinates[1].x;
        float uy1 = charInfo.textureCoordinates[1].y;

        int index = size * 7;
        vertices[index] = x1;      vertices[index + 1] = y;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux1; vertices[index + 6] = uy0;

        index += 7;
        vertices[index] = x1;      vertices[index + 1] = y1;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux1; vertices[index + 6] = uy1;

        index += 7;
        vertices[index] = x;      vertices[index + 1] = y1;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux0; vertices[index + 6] = uy1;

        index += 7;
        vertices[index] = x;      vertices[index + 1] = y;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux0; vertices[index + 6] = uy0;

        size += 4;
        return true;
    }

    public void addText(String text) {
        float x = 0;
        for (int i=0; i < text.length(); i++) {
            char c = text.charAt(i);

            CharInfo charInfo = font.getCharacter(c);
            if (charInfo.width == 0) {
                System.out.println("Unknown character " + c);
                continue;
            }

            float xPos = x;
            addCharacter(xPos, 0, charInfo);
            x += charInfo.width;
        }
    }
}
