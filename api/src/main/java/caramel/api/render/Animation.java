package caramel.api.render;

import caramel.api.texture.Sprite;

public final class Animation {
    public String name;
    public int start;
    public int end;

    private final Sprite[] sprites;

    public Animation(Sprite[] sprites) {
        this.sprites = sprites;
    }

    public Sprite[] getSprites() {
        return sprites;
    }

    @Override
    public String toString() {
        return name + ": [" + start + "-" + end + "]";
    }
}
