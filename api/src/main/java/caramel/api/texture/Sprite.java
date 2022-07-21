package caramel.api.texture;

import caramel.api.components.Camera;
import caramel.api.components.Transform;
import org.joml.Vector2f;
import org.joml.Vector4f;

public final class Sprite {
    private Vector4f color;
    private String shader;
    private Mesh mesh;
    private Texture texture;
    private Vector2f[] texCoords = {
            new Vector2f(1, 1),
            new Vector2f(1, 0),
            new Vector2f(0, 1),
            new Vector2f(0, 0)
    };

    public Sprite(Texture texture) {
        mesh = MeshBuilder.createQuad(1);
        mesh.setTexture(texture.getPath());
        this.texture = Texture.getTexture(texture.getPath());
        shader = "default";
        color = new Vector4f(1, 1, 1, 1);
    }

    public Vector4f getColor() {
        return color;
    }

    public void setColor(Vector4f color) {
        this.color = color;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(String path) {
        mesh.setTexture(path);
        this.texture = Texture.getTexture(path);
    }

    public void setShader(String shader) {
        this.shader = shader;
    }

    public String getShader() {
        return shader;
    }

    public void render(Transform transform, Camera camera) {
        mesh.render(transform, camera);
    }
}
