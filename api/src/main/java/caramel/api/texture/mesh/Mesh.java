package caramel.api.texture.mesh;

import caramel.api.components.Camera;
import caramel.api.components.Transform;
import caramel.api.graphics.Graphics;
import caramel.api.interfaces.Copyable;
import caramel.api.math.Vertex;
import caramel.api.render.BatchRenderer;
import caramel.api.render.MeshRenderer;
import caramel.api.render.Shader;
import caramel.api.texture.Material;
import caramel.api.texture.Texture;
import caramel.api.utils.Color;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static caramel.api.graphics.GL20.GL_ARRAY_BUFFER;
import static caramel.api.graphics.GL20.GL_DYNAMIC_DRAW;
import static caramel.api.graphics.GL20.GL_ELEMENT_ARRAY_BUFFER;
import static caramel.api.graphics.GL20.GL_FLOAT;
import static caramel.api.graphics.GL20.GL_TEXTURE0;
import static caramel.api.graphics.GL20.GL_TRIANGLES;
import static caramel.api.graphics.GL20.GL_UNSIGNED_INT;

public class Mesh implements Copyable<Mesh> {
    public static final Class<? extends Mesh>[] MESHES = new Class[] {
            CircleMesh.class, QuadMesh.class, TriangleMesh.class, CubeMesh.class, IcosahedronMesh.class
    };
    protected transient final List<Vertex> dirtyVertexArray;
    protected transient int vaoId, vboId, eboId;
    protected final List<Vertex> vertexArray;
    protected final List<Integer> elementArray;

    protected String shader;
    protected String texture;
    protected Color color = new Color();
    protected boolean dirty = false;
    protected boolean drawArrays = false;
    protected boolean withIndices = false;

    public final Material material;
    public String name;
    public int type;

    public Mesh() {
        name = "Custom";
        vertexArray = new ArrayList<>();
        dirtyVertexArray = new ArrayList<>();
        elementArray = new ArrayList<>(6);
        type = GL_TRIANGLES;
        material = new Material();
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

    @Override
    public Mesh copy() {
        Mesh mesh = new Mesh();
        mesh.name = name;
        mesh.drawArrays = drawArrays;
        mesh.type = type;
        mesh.texture = texture;
        mesh.shader = shader;
        mesh.dirty = dirty;
        for (Vertex vertex : vertexArray) {
            Vertex copy = new Vertex();
            copy.position.set(vertex.position);
            copy.normal.set(vertex.normal);
            copy.color.set(vertex.color);
            copy.texCoords.set(vertex.texCoords);
            copy.texSlot = vertex.texSlot;
            mesh.vertexArray.add(copy);
        }
        mesh.elementArray.addAll(elementArray);
        mesh.build(withIndices);
        return mesh;
    }

    public Mesh resetArrays() {
        vertexArray.clear();
        setDirty(true);
        return this;
    }

    public Mesh resetDirtyArrays() {
        dirtyVertexArray.clear();
        setDirty(true);
        return this;
    }

    public Mesh resetIndices() {
        elementArray.clear();
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

    public int getIndex(int index) {
        return elementArray.get(index);
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
        this.color.set(color);
        for (Vertex vertex : vertexArray) {
            vertex.color.set(color);
        }
        if (texture != null) {
            shader = "default";
        } else {
            shader = "color";
        }
        dirty = true;
    }

    public void build() {
        build(true);
    }

    public void build(boolean with_indices) {
        this.withIndices = with_indices;
        if (name == null) {
            name = "Custom";
        }
        if (shader == null) {
            if (texture != null) {
                shader = "default";
            } else {
                shader = "color";
            }
        }

        vaoId = Graphics.get().glGenVertexArrays();
        Graphics.get().glBindVertexArray(vaoId);

        vboId = Graphics.get().glGenBuffers();
        Graphics.get().glBindBuffer(GL_ARRAY_BUFFER, vboId);
        Graphics.get().glBufferData(GL_ARRAY_BUFFER, getVertexBuffer(), GL_DYNAMIC_DRAW);

        if (with_indices) {
            eboId = Graphics.get().glGenBuffers();
            Graphics.get().glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
            Graphics.get().glBufferData(GL_ELEMENT_ARRAY_BUFFER, getIndexBuffer(), GL_DYNAMIC_DRAW);
        }

        int positionSize = 3;
        int colorSize = 4;
        int texSize = 2;
        int normalSize = 3;
        int texIdSize = 1;
        int floatSizeBytes = 4;
        int vertexSizeBytes = (positionSize + colorSize + texSize + normalSize + texIdSize) * floatSizeBytes;

        Graphics.get().glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
        Graphics.get().glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionSize * floatSizeBytes);
        Graphics.get().glVertexAttribPointer(2, texSize, GL_FLOAT, false, vertexSizeBytes, positionSize * floatSizeBytes + colorSize * floatSizeBytes);
        Graphics.get().glVertexAttribPointer(3, normalSize, GL_FLOAT, false, vertexSizeBytes,positionSize * floatSizeBytes + colorSize * floatSizeBytes + texSize * floatSizeBytes);
        Graphics.get().glVertexAttribPointer(4, texIdSize, GL_FLOAT, false, vertexSizeBytes,positionSize * floatSizeBytes + colorSize * floatSizeBytes + texSize * floatSizeBytes + normalSize * floatSizeBytes);
        Graphics.get().glBindVertexArray(0);

        if (texture != null) {
            Texture.getTexture(texture);
        }

        MeshRenderer.addMesh(this);
    }

    public void invalidate() {
        Graphics.get().glDeleteVertexArrays(vaoId);
        Graphics.get().glDeleteBuffers(vboId);
        vaoId = 0;
        vboId = 0;
        if (eboId != 0) {
            Graphics.get().glDeleteBuffers(eboId);
            eboId = 0;
        }
    }

    public float[] getVertexBuffer() {
        float[] vertices = new float[vertexArray.size() * Vertex.SIZE];
        int index = 0;
        for (Vertex v : vertexArray) {
            vertices[  index  ] = v.position.x;
            vertices[index + 1] = v.position.y;
            vertices[index + 2] = v.position.z;

            vertices[index + 3] = v.color.r;
            vertices[index + 4] = v.color.g;
            vertices[index + 5] = v.color.b;
            vertices[index + 6] = v.color.a;

            vertices[index + 7] = v.texCoords.x;
            vertices[index + 8] = v.texCoords.y;

            vertices[index + 9] = v.normal.x;
            vertices[index + 10] = v.normal.y;
            vertices[index + 11] = v.normal.z;

            vertices[index + 12] = v.texSlot;
            index += Vertex.SIZE;
        }

        return vertices;
    }

    public int[] getIndexBuffer() {
        int[] indices = new int[elementArray.size()];
        for (int i = 0; i < elementArray.size(); i++) {
            indices[i] = elementArray.get(i);
        }
        return indices;
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
        Matrix4f mvp = transform.getModel();
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

        Vector4f aPos = new Vector4f();
        for (Vertex vertex : vertexArray) {
            Vertex v = new Vertex();
            aPos.set(vertex.position, 1f);
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
            Graphics.get().glActiveTexture(GL_TEXTURE0);
            tex.bind();
            s.uploadTexture("texSampler", 0);
        }

        if (dirty) {
            Graphics.get().glBindBuffer(GL_ARRAY_BUFFER, vboId);
            Graphics.get().glBufferSubData(GL_ARRAY_BUFFER, 0, getVertexBuffer());
            if (withIndices) {
                Graphics.get().glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
                Graphics.get().glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, getIndexBuffer());
            }
            dirty = false;
        }

        Matrix4f mvp = camera.getProjection().mul(camera.getView()).mul(transform.getModel());
        s.uploadMat4f("uMVP", mvp);

        Graphics.get().glBindVertexArray(vaoId);
        Graphics.get().glEnableVertexAttribArray(0);
        Graphics.get().glEnableVertexAttribArray(1);
        Graphics.get().glEnableVertexAttribArray(2);
        Graphics.get().glEnableVertexAttribArray(3);
        Graphics.get().glEnableVertexAttribArray(4);

        if (drawArrays) Graphics.get().glDrawArrays(type, 0, vertexArray.size());
        else Graphics.get().glDrawElements(type, elementArray.size(), GL_UNSIGNED_INT, 0);
        BatchRenderer.DRAW_CALLS++;

        Graphics.get().glDisableVertexAttribArray(0);
        Graphics.get().glDisableVertexAttribArray(1);
        Graphics.get().glDisableVertexAttribArray(2);
        Graphics.get().glDisableVertexAttribArray(3);
        Graphics.get().glDisableVertexAttribArray(4);
        Graphics.get().glBindVertexArray(0);

        if (texture != null) {
            Texture tex = Texture.getTexture(texture);
            tex.unbind();
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
