package caramel.api.texture;

import org.joml.Vector2f;

public final class Sprite {
    private final Vector2f[] texCoords;

    public Sprite(Vector2f[] coords) {
        this.texCoords = coords;
    }

    public Vector2f[] getTexCoords() {
        return texCoords;
    }
}
