package caramel.api.render;

import caramel.api.Time;
import caramel.api.components.Camera;
import caramel.api.debug.Debug;
import caramel.api.interfaces.FunctionButton;
import caramel.api.objects.GameObject;
import caramel.api.texture.Spritesheet;
import caramel.api.texture.Texture;

import java.io.File;

public final class SpriteRenderer extends Renderer {
    public Spritesheet spritesheet;
    public Texture texture;
    public int index = 0;
    public String animation = "";
    public int columns = 1;
    public int rows = 1;
    public float timePerAnimation = 1f;

    private transient float timeElapsed = 0;

    public SpriteRenderer(GameObject gameObject) {
        super(gameObject);
    }

    @FunctionButton
    public void stepAnimation() {
        if (spritesheet == null) return;
        spritesheet.step();
    }

    @FunctionButton
    public void setAnimation() {
        if (spritesheet == null) return;
        spritesheet.setCurrentAnimation(animation);
    }

    public void setAnimation(String animation) {
        this.animation = animation;
        if (spritesheet == null) return;
        spritesheet.setCurrentAnimation(animation);
    }

    public void buildAnimation() {
        if (texture == null) return;
        if (spritesheet != null) {
            spritesheet.invalidate();
        }
        spritesheet = new Spritesheet(texture.getPath(), columns, rows);
        spritesheet.build();
    }

    @Override
    public void render(Camera camera) {
        if (spritesheet == null) {
            buildAnimation();
        }
        if (spritesheet != null) {
            index = spritesheet.currentIndex;
            spritesheet.render(transform, camera);
        }
    }

    @Override
    public void start() {
        if (spritesheet == null) {
            buildAnimation();
        }
    }

    @Override
    public void update() {
        if (spritesheet == null) return;
        if (timePerAnimation > 0) {
            timeElapsed += Time.deltaTime;
            if (timeElapsed > timePerAnimation) {
                timeElapsed = 0;
                spritesheet.step();
            }
        }
    }
}
