package caramel.api.render;

import caramel.api.components.Camera;
import caramel.api.debug.Debug;
import caramel.api.interfaces.FunctionButton;
import caramel.api.objects.GameObject;
import caramel.api.texture.Spritesheet;

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
