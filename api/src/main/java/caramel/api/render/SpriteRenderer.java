package caramel.api.render;

import caramel.api.Time;
import caramel.api.components.Camera;
import caramel.api.interfaces.FunctionButton;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.objects.GameObject;
import caramel.api.texture.Spritesheet;
import caramel.api.texture.Texture;

public final class SpriteRenderer extends Renderer {
    public transient Spritesheet spritesheet;
    @InvokeOnEdit("buildAnimation") public Texture texture;

    @InvokeOnEdit("setIndex") public int index = 0;
    @InvokeOnEdit("setAnim") public String animation = "";
    public int columns = 1;
    public int rows = 1;
    public float timePerAnimation = 1f;

    private transient float timeElapsed = 0;

    public SpriteRenderer(final GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void build() {
        if (spritesheet == null) {
            buildAnimation();
        } else {
            spritesheet.build();
        }
    }

    public void setIndex() {
        spritesheet.setCurrentIndex(index);
    }

    public void setAnim() {
        if (spritesheet == null) return;
        spritesheet.setCurrentAnimation(animation);
    }

    public void setAnimation(final String animation) {
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

    @FunctionButton
    public void step() {
        if (spritesheet == null) return;
        spritesheet.step();
    }

    @Override
    public void render(final Camera camera) {
        if (spritesheet == null) {
            buildAnimation();
        }
        if (spritesheet != null) {
            index = spritesheet.getCurrentIndex();
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

    @Override
    public SpriteRenderer clone(final GameObject gameObject, final boolean copyId) {
        final SpriteRenderer clone = (SpriteRenderer) super.clone(gameObject, copyId);
        clone.spritesheet = spritesheet.copy();
        return clone;
    }
}
