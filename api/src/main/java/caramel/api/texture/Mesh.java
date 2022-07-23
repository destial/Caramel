package caramel.api.texture;

import caramel.api.components.Camera;
import caramel.api.components.Transform;
import caramel.api.debug.Debug;
import caramel.api.math.Vertex;
import caramel.api.render.BatchRenderer;
import caramel.api.render.Shader;
import caramel.api.utils.Color;
import org.joml.Matrix4f;
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

public class Mesh {

    protected final List<Vertex> vertexArray;
    protected final List<Vertex> dirtyVertexArray;
    protected final List<Integer> elementArray;

    public int type;

    protected String shader;
    protected String texture;
    protected Color color;
    protected transient int vaoId, vboId, eboId;
    protected boolean dirty = false;
    protected boolean drawArrays = false;
    protected boolean withIndices = false;

    public Mesh() {
        vertexArray = new ArrayList<>();
        dirtyVertexArray = new ArrayList<>();
        elementArray = new ArrayList<>(6);
        type = GL_TRIANGLES;
    }

    public List<Vertex> getVertexArray() {
        return vertexArray;
    }

    public List<Vertex> getDirtyVertexArray() {
        return dirtyVertexArray;
    }

    public List<Integer> getElementArray() {
        return elementArray;
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
            pushVertex(0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 0f);
            if (index < vertexArray.size()) break;
        }
        return vertexArray.get(index);
    }

    public Mesh pushVertex(Vector3f position, Color color, Vector2f texCoords, Vector3f normal, float texSlot) {
        Vertex vertex = new Vertex();
        vertex.position.set(position);
        vertex.color.set(color);
        vertex.texCoords.set(texCoords);
        vertex.normal.set(normal);
        vertex.texSlot = texSlot;
        setDirty(true);
        return pushVertex(vertex);
    }

    public Mesh pushVertex(float... floats) {
        if (floats.length != Vertex.SIZE) return this;
        Vertex vertex = new Vertex();
        vertex.position.x = floats[0];
        vertex.position.y = floats[1];
        vertex.position.z = floats[2];

        vertex.color.r = floats[3];
        vertex.color.g = floats[4];
        vertex.color.b = floats[5];
        vertex.color.a = floats[6];

        vertex.texCoords.x = floats[7];
        vertex.texCoords.y = floats[8];

        vertex.normal.x = floats[9];
        vertex.normal.y = floats[10];
        vertex.normal.z = floats[11];

        vertex.texSlot = floats[12];
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
                vertex.color.set(color);
            }
            if (texture != null) {
                shader = "default";
            } else {
                shader = "color";
            }
        }
        dirty = true;
    }

    public void build() {
        build(true);
    }

    public void build(boolean with_indices) {
        this.withIndices = with_indices;
        if (shader == null) {
            if (texture != null) {
                shader = "default";
            } else {
                shader = "color";
            }
        }

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, getVertexBuffer(), GL_DYNAMIC_DRAW);

        if (with_indices) {
            eboId = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, getIndexBuffer(), GL_DYNAMIC_DRAW);
        }

        int positionSize = 3;
        int colorSize = 4;
        int texSize = 2;
        int normalSize = 3;
        int texIdSize = 1;
        int floatSizeBytes = 4;
        int vertexSizeBytes = (positionSize + colorSize + texSize + normalSize + texIdSize) * floatSizeBytes;

        glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionSize * floatSizeBytes);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, texSize, GL_FLOAT, false, vertexSizeBytes, positionSize * floatSizeBytes + colorSize * floatSizeBytes);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, normalSize, GL_FLOAT, false, vertexSizeBytes,positionSize * floatSizeBytes + colorSize * floatSizeBytes + texSize * floatSizeBytes);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(4, texIdSize, GL_FLOAT, false, vertexSizeBytes,positionSize * floatSizeBytes + colorSize * floatSizeBytes + texSize * floatSizeBytes + normalSize * floatSizeBytes);
        glEnableVertexAttribArray(4);

        glBindVertexArray(0);

        if (texture != null) {
            Texture.getTexture(texture);
        }
    }

    public FloatBuffer getVertexBuffer() {
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.size() * Vertex.SIZE);
        float[] vertex = new float[vertexArray.size() * Vertex.SIZE];
        int index = 0;
        for (Vertex v : vertexArray) {
            vertex[  index  ] = v.position.x;
            vertex[index + 1] = v.position.y;
            vertex[index + 2] = v.position.z;

            vertex[index + 3] = v.color.r;
            vertex[index + 4] = v.color.g;
            vertex[index + 5] = v.color.b;
            vertex[index + 6] = v.color.a;

            vertex[index + 7] = v.texCoords.x;
            vertex[index + 8] = v.texCoords.y;

            vertex[index + 9] = v.normal.x;
            vertex[index + 10] = v.normal.y;
            vertex[index + 11] = v.normal.z;

            vertex[index + 12] = v.texSlot;
            index += Vertex.SIZE;
        }
        vertexBuffer.put(vertex).flip();
        return vertexBuffer;
    }

    public IntBuffer getIndexBuffer() {
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.size());
        int[] indices = new int[elementArray.size()];
        for (int i = 0; i < elementArray.size(); i++) {
            indices[i] = elementArray.get(i);
        }
        elementBuffer.put(indices).flip();
        return elementBuffer;
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
            } else {
                shader = "color";
            }
        } else {
            shader = "color";
        }
        this.dirty = true;
    }

    public String getTexturePath() {
        return texture;
    }

    public Texture getTexture() {
        return texture != null && !texture.isEmpty() ? Texture.getTexture(texture) : null;
    }

    public void renderBatch(Transform transform, Camera camera) {
        shader = "defaultBatch";
        Shader s = Shader.getShader(shader);
        Matrix4f mvp = camera.getProjection().mul(camera.getView()).mul(transform.getModel());

        dirtyVertexArray.clear();
        Texture t = texture != null ? Texture.getTexture(texture) : null;
        int texId = -1;
        if (t != null) {
            List<Texture> textures = Texture.getTextures();
            for (int i = 0; i < textures.size(); i++) {
                if (t == textures.get(i)) {
                    texId = i;
                    break;
                }
            }
        }

        for (Vertex vertex : vertexArray) {
            Vertex v = new Vertex();
            Vector4f aPos = new Vector4f(vertex.position, 1f);
            aPos.mul(mvp);
            v.position.x = aPos.x;
            v.position.y = aPos.y;
            v.position.z = aPos.z;
            v.texCoords.set(vertex.texCoords);
            v.normal.set(vertex.normal);
            v.color.set(vertex.color);
            v.texSlot = texId;

            dirtyVertexArray.add(v);
        }

        BatchRenderer.addMesh(s, this);
    }

    public void render(Transform transform, Camera camera) {
        if (shader.equals("defaultBatch")) {
            if (texture != null) {
                Texture tex = Texture.getTexture(texture);
                if (tex != null) {
                    shader = "default";
                } else {
                    shader = "color";
                }
            } else {
                shader = "color";
            }
        }

        Shader s = Shader.getShader(shader);
        s.attach();
        if (texture != null) {
            Texture tex = Texture.getTexture(texture);
            glActiveTexture(GL_TEXTURE0);
            tex.bind();
            s.uploadTexture("texSampler", 0);
        }

        if (dirty) {
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferSubData(GL_ARRAY_BUFFER, 0, getVertexBuffer());
            if (withIndices) {
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
                glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, getIndexBuffer());
            }
            dirty = false;
        }

        Matrix4f mvp = camera.getProjection().mul(camera.getView()).mul(transform.getModel());
        s.uploadMat4f("uMVP", mvp);

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);

        if (drawArrays) glDrawArrays(type, 0, vertexArray.size());
        else glDrawElements(type, elementArray.size(), GL_UNSIGNED_INT, 0);

        Debug.log(toString());

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);

        glBindVertexArray(0);

        if (texture != null) {
            Texture tex = Texture.getTexture(texture);
            tex.unbind();
            glActiveTexture(0);
        }

        s.detach();
    }

    @Override
    public String toString() {
        return "{" +
                "vertexArray=" + vertexArray +
                ", elementArray=" + elementArray +
                ", type=" + type +
                ", dirty=" + dirty +
                ", withIndices=" + withIndices +
                '}';
    }
}
