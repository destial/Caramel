package caramel.api.render;

import caramel.api.Time;
import caramel.api.components.Camera;
import caramel.api.debug.Debug;
import caramel.api.interfaces.FunctionButton;
import caramel.api.objects.GameObject;
import caramel.api.texture.Spritesheet;

public final class SpriteRenderer extends Renderer {
    public Spritesheet spritesheet;
    public int index = 0;
    public String animation = "";
    public float timePerAnimation = 1f;

    private transient float timeElapsed = 0;

    public SpriteRenderer(GameObject gameObject) {
        super(gameObject);
    }

    @FunctionButton
    public void stepAnimation() {
        spritesheet.step();
    }

    public void buildAnimation() {
        spritesheet = new Spritesheet("assets/textures/player.png", 3, 6);
        spritesheet.build();
    }

    @Override
    public void render(Camera camera) {
        if (spritesheet == null) {
            buildAnimation();
        }

        spritesheet.render(transform, camera);
    }

    @FunctionButton
    public void setAnimation() {
        spritesheet.setCurrentAnimation(animation);
    }

    @Override
    public void start() {
        if (spritesheet == null) {
            buildAnimation();
        }

        spritesheet.addAnimation("look left", 0, 2);
    }

    @Override
    public void update() {
        timeElapsed += Time.deltaTime;
        if (timeElapsed > timePerAnimation) {
            timeElapsed = 0;
            spritesheet.step();
        }
    }
}
