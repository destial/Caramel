package caramel.api.render;

import caramel.api.components.Camera;
import caramel.api.graphics.Graphics;
import caramel.api.math.Vertex;
import caramel.api.texture.mesh.Mesh;
import caramel.api.texture.Texture;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static caramel.api.graphics.GL20.GL_ARRAY_BUFFER;
import static caramel.api.graphics.GL20.GL_ELEMENT_ARRAY_BUFFER;
import static caramel.api.graphics.GL20.GL_TEXTURE0;
import static caramel.api.graphics.GL20.GL_TRIANGLES;
import static caramel.api.graphics.GL20.GL_UNSIGNED_INT;

public final class BatchRenderer extends Mesh {
    public static final int MAX_TEXTURES = 8;
    public static final int MAX_BATCH_SIZE = 1000;
    public static boolean USE_BATCH = false;
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
        for (int i = 0; i < MAX_BATCH_SIZE * 4; i++) {
            vertexArray.add(new Vertex());
        }
        for (int i = 0; i < MAX_BATCH_SIZE * 6; i++) {
            elementArray.add(0);
        }

        super.build();

        vertexArray.clear();
        elementArray.clear();
    }

    public static void addMesh(final Shader shader, final Mesh mesh) {
        if (shaderMapping == null) {
            shaderMapping = new HashMap<>();
            final List<BatchRenderer> rendererList = new ArrayList<>();
            final BatchRenderer batchRenderer = new BatchRenderer();
            batchRenderer.build();
            rendererList.add(batchRenderer);
            shaderMapping.put(shader, rendererList);
        }
        List<BatchRenderer> rendererList = shaderMapping.get(shader);
        if (rendererList == null || rendererList.isEmpty()) {
            if (rendererList == null) rendererList = new ArrayList<>();
            final BatchRenderer batchRenderer = new BatchRenderer();
            batchRenderer.build();
            rendererList.add(batchRenderer);
            shaderMapping.put(shader, rendererList);
        }

        BatchRenderer renderer = rendererList.get(rendererList.size() - 1);
        final List<Vertex> dirty = mesh.getDirtyVertexArray();
        if (dirty.isEmpty()) return;

        // Generate new batch if out of textures or out of vertex buffers
        final Vertex lastVertex = dirty.get(dirty.size() - 1);
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
        final int indices = renderer.elementArray.stream().reduce((a, b) -> a > b ? a : b).orElse(-1);
        for (int element : mesh.getElementArray()) {
            renderer.elementArray.add(indices + element + 1);
        }

        renderer.setDirty(true);
    }

    public static void invalidateAll() {
        if (shaderMapping == null) return;
        for (final Map.Entry<Shader, List<BatchRenderer>> entry : shaderMapping.entrySet()) {
            for (final BatchRenderer renderer : entry.getValue()) {
                renderer.invalidate();
            }
            entry.getValue().clear();
        }
        shaderMapping.clear();
    }

    public static void render(Camera camera) {
        if (shaderMapping == null) return;
        final int[] texSlots = new int[MAX_TEXTURES];
        for (int t = 0; t < MAX_TEXTURES; t++) {
            texSlots[t] = t;
        }

        final Matrix4f uVP = camera.getProjection().mul(camera.getView());

        for (final Map.Entry<Shader, List<BatchRenderer>> entry : shaderMapping.entrySet()) {
            final Shader shader = entry.getKey();
            final List<BatchRenderer> rendererList = entry.getValue();
            shader.attach();
            shader.uploadIntArray("texSampler", texSlots);
            shader.uploadMat4f("uVP", uVP);

            for (int index = 0; index < rendererList.size(); index++) {
                final BatchRenderer renderer = rendererList.get(index);
                if (!renderer.dirty) {
                    renderer.invalidate();
                    rendererList.remove(renderer);
                    index--;
                    continue;
                }

                final List<Texture> textures = Texture.getTextures();
                final int offset = index * MAX_TEXTURES;
                for (int i = offset; i < textures.size() && i + offset < MAX_TEXTURES; i++) {
                    Graphics.get().glActiveTexture(GL_TEXTURE0 + i - offset);
                    textures.get(i).bind();
                }

                Graphics.get().glBindBuffer(GL_ARRAY_BUFFER, renderer.vboId);
                Graphics.get().glBufferSubData(GL_ARRAY_BUFFER, 0, renderer.getVertexBuffer());
                Graphics.get().glBindBuffer(GL_ARRAY_BUFFER, 0);
                if (renderer.withIndices) {
                    Graphics.get().glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, renderer.eboId);
                    Graphics.get().glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, renderer.getIndexBuffer());
                    Graphics.get().glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
                }

                Graphics.get().glBindVertexArray(renderer.vaoId);
                Graphics.get().glEnableVertexAttribArray(0);
                Graphics.get().glEnableVertexAttribArray(1);
                Graphics.get().glEnableVertexAttribArray(2);
                Graphics.get().glEnableVertexAttribArray(3);
                Graphics.get().glEnableVertexAttribArray(4);

                Graphics.get().glDrawElements(GL_TRIANGLES, renderer.elementArray.size(), GL_UNSIGNED_INT, 0);
                DRAW_CALLS++;

                Graphics.get().glDisableVertexAttribArray(0);
                Graphics.get().glDisableVertexAttribArray(1);
                Graphics.get().glDisableVertexAttribArray(2);
                Graphics.get().glDisableVertexAttribArray(3);
                Graphics.get().glDisableVertexAttribArray(4);

                Graphics.get().glBindVertexArray(0);

                for (int i = offset; i < textures.size() && i + offset < MAX_TEXTURES; i++) {
                    textures.get(i).unbind();
                }

                // flush
                renderer.resetArrays();
                renderer.resetIndices();
                renderer.setDirty(false);
            }
            shader.detach();
        }
    }
}
