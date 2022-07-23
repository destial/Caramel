package caramel.api.render;

import caramel.api.math.Vertex;
import caramel.api.texture.Mesh;
import caramel.api.texture.Texture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public final class BatchRenderer extends Mesh {
    public static final int MAX_TEXTURES = 8;
    public static final int MAX_BATCH_SIZE = 1000;
    public static boolean USE_BATCH = true;
    public static int DRAW_CALLS = 0;
    private static Map<Shader, List<BatchRenderer>> shaderMapping;

    private BatchRenderer() {}

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

    @Override
    public void build() {
        for (int i = 0; i < MAX_BATCH_SIZE * 3; i++) {
            vertexArray.add(new Vertex());
        }
        for (int i = 0; i < MAX_BATCH_SIZE * 6; i++) {
            elementArray.add(0);
        }

        super.build();

        vertexArray.clear();
        elementArray.clear();
    }

    public static void addMesh(Shader shader, Mesh mesh) {
        if (shaderMapping == null) {
            shaderMapping = new HashMap<>();
            List<BatchRenderer> rendererList = new ArrayList<>();
            BatchRenderer batchRenderer = new BatchRenderer();
            batchRenderer.build();
            rendererList.add(batchRenderer);
            shaderMapping.put(shader, rendererList);
        }
        List<BatchRenderer> rendererList = shaderMapping.get(shader);
        if (rendererList == null || rendererList.isEmpty()) {
            if (rendererList == null) rendererList = new ArrayList<>();
            BatchRenderer batchRenderer = new BatchRenderer();
            batchRenderer.build();
            rendererList.add(batchRenderer);
            shaderMapping.put(shader, rendererList);
        }

        BatchRenderer renderer = rendererList.get(rendererList.size() - 1);
        List<Vertex> dirty = mesh.getDirtyVertexArray();
        if (dirty.isEmpty()) return;

        // Generate new batch if out of textures or out of vertex buffers
        Vertex lastVertex = dirty.get(dirty.size() - 1);
        if (lastVertex.texSlot >= MAX_TEXTURES) {
            renderer = new BatchRenderer();
            renderer.build();
            rendererList.add(renderer);
            for (Vertex vertex : dirty) {
                vertex.texSlot -= MAX_TEXTURES;
            }
        } else if (renderer.vertexArray.size() + dirty.size() > MAX_BATCH_SIZE * 4) {
            renderer = new BatchRenderer();
            renderer.build();
            rendererList.add(renderer);
        }  else if (renderer.elementArray.size() + mesh.getElementArray().size() > MAX_BATCH_SIZE * 6) {
            renderer = new BatchRenderer();
            renderer.build();
            rendererList.add(renderer);
        }

        // Add mesh vertices and index buffers to batch
        renderer.vertexArray.addAll(dirty);
        int indices = renderer.elementArray.stream().reduce((a, b) -> a > b ? a : b).orElse(-1);
        for (int element : mesh.getElementArray()) {
            renderer.elementArray.add(indices + element + 1);
        }

        renderer.setDirty(true);
    }

    public static void render() {
        if (shaderMapping == null) return;
        int[] texSlots = new int[MAX_TEXTURES];
        for (int t = 0; t < MAX_TEXTURES; t++) {
            texSlots[t] = t;
        }

        for (Map.Entry<Shader, List<BatchRenderer>> entry : shaderMapping.entrySet()) {
            Shader shader = entry.getKey();
            List<BatchRenderer> rendererList = entry.getValue();

            for (int index = 0; index < rendererList.size(); index++) {
                BatchRenderer renderer = rendererList.get(index);
                if (!renderer.dirty) {
                    renderer.invalidate();
                    rendererList.remove(renderer);
                    index--;
                    continue;
                }

                shader.attach();
                List<Texture> textures = Texture.getTextures();
                int offset = index * MAX_TEXTURES;
                for (int i = offset; i < textures.size() && i + offset < MAX_TEXTURES; i++) {
                    glActiveTexture(GL_TEXTURE0 + i - offset);
                    textures.get(i).bind();
                }
                shader.uploadIntArray("texSampler", texSlots);

                glBindBuffer(GL_ARRAY_BUFFER, renderer.vboId);
                float[] vertexBuffer = renderer.getVertexBuffer();
                glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
                if (renderer.withIndices) {
                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, renderer.eboId);
                    int[] indexBuffer = renderer.getIndexBuffer();
                    glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indexBuffer);
                }

                glBindVertexArray(renderer.vaoId);
                glEnableVertexAttribArray(0);
                glEnableVertexAttribArray(1);
                glEnableVertexAttribArray(2);
                glEnableVertexAttribArray(3);
                glEnableVertexAttribArray(4);

                glDrawElements(GL_TRIANGLES, renderer.elementArray.size(), GL_UNSIGNED_INT, 0);
                DRAW_CALLS++;

                glDisableVertexAttribArray(0);
                glDisableVertexAttribArray(1);
                glDisableVertexAttribArray(2);
                glDisableVertexAttribArray(3);
                glDisableVertexAttribArray(4);

                glBindVertexArray(0);

                for (int i = offset; i < textures.size() && i + offset < MAX_TEXTURES; i++) {
                    textures.get(i).unbind();
                }

                // flush
                shader.detach();

                renderer.resetArrays();
                renderer.resetIndices();
                renderer.setDirty(false);
            }
        }
    }
}
