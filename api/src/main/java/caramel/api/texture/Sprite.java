package caramel.api.texture;

import caramel.api.interfaces.Copyable;
import org.joml.Vector2f;

public final class Sprite implements Copyable<Sprite> {
    private final Vector2f[] texCoords;

    public Sprite(Vector2f[] coords) {
        this.texCoords = coords;
    }

    public Vector2f[] getTexCoords() {
        return texCoords;
    }

    @Override
    public Sprite copy() {
        return new Sprite(texCoords);
    }
}
