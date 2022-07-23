package caramel.api.render;

import caramel.api.debug.Debug;
import caramel.api.math.Vertex;
import caramel.api.texture.Mesh;
import caramel.api.texture.Texture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static boolean USE_BATCH = false;
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

        Vertex lastVertex = dirty.get(dirty.size() - 1);
        if (lastVertex.texSlot >= MAX_TEXTURES) {
            renderer = new BatchRenderer();
            renderer.build();
            rendererList.add(renderer);
            for (Vertex vertex : dirty) {
                vertex.texSlot -= MAX_TEXTURES;
            }
        }
        renderer.vertexArray.addAll(dirty);
        int indices = renderer.elementArray.stream().reduce((a, b) -> a > b ? a : b).orElse(0);
        if (!renderer.elementArray.isEmpty()) {
            indices += 1;
        }
        for (int element : mesh.getElementArray()) {
            renderer.elementArray.add(indices + element);
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
            // render

            for (int index = 0; index < rendererList.size(); index++) {
                shader.attach();
                BatchRenderer renderer = rendererList.get(index);

                if (renderer.dirty) {
                    glBindBuffer(GL_ARRAY_BUFFER, renderer.vboId);
                    glBufferSubData(GL_ARRAY_BUFFER, 0, renderer.getVertexBuffer());
                    if (renderer.withIndices) {
                        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, renderer.eboId);
                        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, renderer.getIndexBuffer());
                    }
                    renderer.dirty = false;
                }

                List<Texture> textures = Texture.getTextures();
                int a = 0;
                int offset = index * MAX_TEXTURES;
                for (int i = offset; i < textures.size() && i + offset < MAX_TEXTURES; i++) {
                    glActiveTexture(GL_TEXTURE0 + (++a));
                    textures.get(i).bind();
                }
                shader.uploadIntArray("texSampler", texSlots);

                glBindVertexArray(renderer.vaoId);
                glEnableVertexAttribArray(0);
                glEnableVertexAttribArray(1);
                glEnableVertexAttribArray(2);
                glEnableVertexAttribArray(3);
                glEnableVertexAttribArray(4);

                glDrawElements(renderer.type, renderer.elementArray.size(), GL_UNSIGNED_INT, 0);

                Debug.log(renderer.toString());

                glDisableVertexAttribArray(0);
                glDisableVertexAttribArray(1);
                glDisableVertexAttribArray(2);
                glDisableVertexAttribArray(3);
                glDisableVertexAttribArray(4);

                glBindVertexArray(0);

                for (int i = index * MAX_TEXTURES; i < textures.size() && i < MAX_TEXTURES; i++) {
                    textures.get(i).unbind();
                }

                glActiveTexture(0);

                // flush
                renderer.resetArrays();
                renderer.resetIndices();

                shader.detach();
            }

            rendererList.clear();
        }
    }
}
