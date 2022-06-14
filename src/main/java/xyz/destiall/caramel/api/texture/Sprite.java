package xyz.destiall.caramel.api.texture;

import org.joml.Vector2f;
import org.joml.Vector4f;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.Transform;

public class Sprite {
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
        this.texture = texture;
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

    public void setTexture(Texture texture) {
        this.texture = texture;
        mesh.setTexture(texture);
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
