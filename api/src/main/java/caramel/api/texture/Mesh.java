package caramel.api.texture;

import caramel.api.components.Camera;
import caramel.api.components.Transform;
import caramel.api.math.Vertex;
import caramel.api.render.Shader;
import caramel.api.utils.Color;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL30.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL30.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL30.GL_FLOAT;
import static org.lwjgl.opengl.GL30.GL_TEXTURE0;
import static org.lwjgl.opengl.GL30.GL_TRIANGLES;
import static org.lwjgl.opengl.GL30.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL30.glActiveTexture;
import static org.lwjgl.opengl.GL30.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glBufferData;
import static org.lwjgl.opengl.GL30.glBufferSubData;
import static org.lwjgl.opengl.GL30.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glDrawArrays;
import static org.lwjgl.opengl.GL30.glDrawElements;
import static org.lwjgl.opengl.GL30.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glGenBuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glVertexAttribPointer;

public final class Mesh {
    private final List<Vertex> vertexArray;
    private final List<Integer> elementArray;

    public int type;

    private String shader;
    private String texture = "";
    private Color color;
    private transient int vaoId, vboId;
    private boolean dirty = false;
    private boolean drawArrays = false;

    public Mesh() {
        vertexArray = new ArrayList<>();
        elementArray = new ArrayList<>(6);
        type = GL_TRIANGLES;
    }

    public void setDrawArrays(boolean drawArrays) {
        this.drawArrays = drawArrays;
    }

    public Mesh(boolean arrays) {
        this();
        drawArrays = arrays;
    }

    public Mesh resetArrays() {
        vertexArray.clear();
        setDirty(true);
        return this;
    }

    public Mesh resetIndices() {
        vertexArray.clear();
        setDirty(true);
        return this;
    }

    public Mesh pushVertex(Vertex vertex) {
        vertexArray.add(vertex);
        setDirty(true);
        return this;
    }

    public Vertex getVertex(int index) {
        while (index >= vertexArray.size()) {
            pushVertex(0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f);
            if (index < vertexArray.size()) break;
        }
        return vertexArray.get(index);
    }

    public Mesh pushVertex(Vector3f position, Vector4f color, Vector2f texCoords, Vector3f normal) {
        Vertex vertex = new Vertex();
        vertex.position.set(position);
        vertex.color.set(color);
        vertex.texCoords.set(texCoords);
        vertex.normal.set(normal);
        setDirty(true);
        return pushVertex(vertex);
    }

    public Mesh pushVertex(float... floats) {
        if (floats.length != Vertex.SIZE) return this;
        Vertex vertex = new Vertex();
        vertex.position.x = floats[0];
        vertex.position.y = floats[1];
        vertex.position.z = floats[2];
        vertex.color.x = floats[3];
        vertex.color.y = floats[4];
        vertex.color.z = floats[5];
        vertex.color.w = floats[6];
        vertex.texCoords.x = floats[7];
        vertex.texCoords.y = floats[8];
        vertex.normal.x = floats[9];
        vertex.normal.y = floats[10];
        vertex.normal.z = floats[11];
        setDirty(true);
        return pushVertex(vertex);
    }

    public Mesh pushIndex(int i) {
        elementArray.add(i);
        setDirty(true);
        return this;
    }

    public Mesh pushIndex(int... indexes) {
        for (int i : indexes) elementArray.add(i);
        setDirty(true);
        return this;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        if (color != null) {
            for (Vertex vertex : vertexArray) {
                vertex.color.set(color.asVector());
            }
            if (shader != null) Shader.getShader(shader).detach();
            shader = "color";
        }
        dirty = true;
    }

    public void build() {
        build(true);
    }

    public void build(boolean with_indices) {
        if (shader == null) {
            if (texture != null) {
                shader = "default";
            } else {
                shader = "color";
            }
        }

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        FloatBuffer vertexBuffer = getVertexBuffer();
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);

        if (with_indices) {
            IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.size());
            int[] indices = new int[elementArray.size()];
            for (int i = 0; i < elementArray.size(); i++) {
                indices[i] = elementArray.get(i);
            }
            elementBuffer.put(indices).flip();
            int eboId = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_DYNAMIC_DRAW);

        }
        int positionSize = 3;
        int colorSize = 4;
        int texSize = 2;
        int normalSize = 3;
        int floatSizeBytes = 4;
        int vertexSizeBytes = (positionSize + colorSize + texSize + normalSize) * floatSizeBytes;
        glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionSize * floatSizeBytes);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, texSize, GL_FLOAT, false, vertexSizeBytes, positionSize * floatSizeBytes + colorSize * floatSizeBytes);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, normalSize, GL_FLOAT, false, vertexSizeBytes,positionSize * floatSizeBytes + colorSize * floatSizeBytes + texSize * floatSizeBytes);
        glEnableVertexAttribArray(3);

        glBindVertexArray(0);

        if (texture != null && !texture.isEmpty()) {
            Texture tex = Texture.getTexture(texture);
            if (tex == null) {
                new Texture(texture).buildTexture();
            } else if (!tex.isLoaded()) {
                tex.buildTexture();
            }
        }
    }

    public FloatBuffer getVertexBuffer() {
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.size() * Vertex.SIZE);
        float[] vertex = new float[vertexArray.size() * Vertex.SIZE];
        int index = 0;
        for (Vertex v : vertexArray) {
            vertex[index] = v.position.x;
            vertex[index + 1] = v.position.y;
            vertex[index + 2] = v.position.z;
            vertex[index + 3] = v.color.x;
            vertex[index + 4] = v.color.y;
            vertex[index + 5] = v.color.z;
            vertex[index + 6] = v.color.w;
            vertex[index + 7] = v.texCoords.x;
            vertex[index + 8] = v.texCoords.y;
            vertex[index + 9] = v.normal.x;
            vertex[index + 10] = v.normal.y;
            vertex[index + 11] = v.normal.z;
            index += Vertex.SIZE;
        }
        vertexBuffer.put(vertex).flip();
        return vertexBuffer;
    }

    public void setShader(String shader) {
        this.shader = shader;
    }

    public Shader getShader() {
        return Shader.getShader(shader);
    }

    public void setTexture(String path) {
        this.texture = path;
        if (texture != null) {
            Texture tex = Texture.getTexture(path);
            if (tex != null) {
                shader = "default";
            }
        }
        this.dirty = true;
    }

    public String getTexturePath() {
        return texture;
    }

    public Texture getTexture() {
        return texture != null && !texture.isEmpty() ? Texture.getTexture(texture) : null;
    }

    public void render(Transform transform, Camera camera) {
        Shader s = Shader.getShader(shader);
        s.use();
        if (texture != null) {
            Texture tex = Texture.getTexture(texture);
            if (tex != null) {
                glActiveTexture(GL_TEXTURE0);
                tex.bind();
                s.uploadTexture("texSampler", 0);
            }
        }

        if (dirty) {
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferSubData(GL_ARRAY_BUFFER, 0, getVertexBuffer());
            dirty = false;
        }

        s.uploadMat4f("uProjection", camera.getProjection());
        s.uploadMat4f("uView", camera.getView());
        s.uploadMat4f("uModel", transform.model);

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        if (drawArrays) glDrawArrays(type, 0, vertexArray.size());
        else glDrawElements(type, elementArray.size(), GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);

        glBindVertexArray(0);

        if (texture != null) {
            Texture tex = Texture.getTexture(texture);
            if (tex != null) {
                tex.unbind();
                glActiveTexture(0);
            }
        }

        s.detach();
    }
}
