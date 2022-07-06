package xyz.destiall.caramel.api.render;

import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.interfaces.FunctionButton;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.texture.Spritesheet;

public final class SpriteRenderer extends Renderer {
    public Spritesheet spritesheet;

    public SpriteRenderer(GameObject gameObject) {
        super(gameObject);
    }

    @FunctionButton
    public void createMesh() {
        Debug.log("mesh boo");
    }

    @Override
    public void render(Camera camera) {
        if (spritesheet != null) spritesheet.render(transform, camera);
    }

    @Override
    public void start() {}

    @Override
    public void update() {}
}
