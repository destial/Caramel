package xyz.destiall.caramel.api.texture;

import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.interfaces.Update;

import java.util.ArrayList;
import java.util.List;

public final class Spritesheet implements Update {
    private int index = 0;
    private final List<Sprite> sprites;
    private final Texture texture;

    public Spritesheet(String path) {
        this.texture = new Texture(path);
        texture.buildTexture();
        sprites = new ArrayList<>();
    }

    public void render(Transform transform, Camera camera) {
        if (sprites.isEmpty()) return;
        if (index > sprites.size()) return;
        sprites.get(index).render(transform, camera);
    }

    @Override
    public void update() {
        Update.super.update();
    }
}
