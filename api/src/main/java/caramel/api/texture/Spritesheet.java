package caramel.api.texture;

import caramel.api.components.Camera;
import caramel.api.components.Transform;
import caramel.api.debug.Debug;
import caramel.api.interfaces.Copyable;
import caramel.api.render.Animation;
import caramel.api.render.BatchRenderer;
import caramel.api.texture.mesh.Mesh;
import caramel.api.texture.mesh.QuadMesh;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Spritesheet implements Copyable<Spritesheet> {
    private transient List<Sprite> sprites;
    private transient boolean loaded = false;

    private Map<String, Animation> animations;
    private final String path;
    private final int columns;
    private final int rows;

    public int currentIndex = 0;
    public String currentAnimation;
    public Mesh mesh;

    public Spritesheet(final String path, final int columns, final int rows) {
        this.columns = columns;
        this.rows = rows;
        this.path = path;
    }

    public void build() {
        if (loaded) return;
        sprites = new ArrayList<>();
        animations = new HashMap<>();
        if (mesh == null) {
            mesh = new QuadMesh(1);
            mesh.setTexture(path);
            mesh.build();
        }
        final Texture texture = mesh.getTexture();
        if (texture == null) return;

        final int spriteHeight = texture.getHeight() / rows;
        final int spriteWidth = texture.getWidth() / columns;

        int currentX = 0;
        int currentY = 0;

        for (int i = 0; i < columns; i++) {
            final float rightX = (currentX + spriteWidth) / (float) texture.getWidth();
            final float topY = (currentY + spriteHeight) / (float) texture.getHeight();
            final float leftX = currentX / (float) texture.getWidth();
            final float bottomY = currentY / (float) texture.getHeight();

            final Vector2f[] texCoords = {
                    new Vector2f(rightX, topY),
                    new Vector2f(leftX, bottomY),
                    new Vector2f(rightX, bottomY),
                    new Vector2f(leftX, topY)
            };

            final Sprite sprite = new Sprite(texCoords);
            sprites.add(sprite);
            currentX += spriteWidth;
            if (currentX >= texture.getWidth()) {
                currentX = 0;
                currentY += spriteHeight;
                i = -1;
            }

            if (currentY >= texture.getHeight()) {
                break;
            }
        }
        autoDetect();
        step();
        loaded = true;
    }

    public void autoDetect() {
        if (sprites.isEmpty() || !animations.isEmpty()) return;

        for (int i = 0; i < rows; i++) {
            final int start = i * columns;
            final int end = start + columns - 1;
            final Animation anim = addAnimation("anim" + i, start, end);
            Debug.log("Auto detected animation " + anim);
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(final int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public Animation addAnimation(final String name, final int indexStart, final int indexEnd) {
        final int total = indexEnd - indexStart;
        final Sprite[] animation = new Sprite[total + 1];
        int j = 0;
        for (int i = indexStart; i <= indexEnd; i++) {
            animation[j++] = sprites.get(i);
        }
        final Animation anim = new Animation(animation);
        anim.name = name;
        anim.start = indexStart;
        anim.end = indexEnd;
        animations.put(name, anim);
        currentAnimation = name;
        currentIndex = -1;
        return anim;
    }

    public void setCurrentAnimation(final String animation) {
        this.currentAnimation = animation;
        this.currentIndex = -1;
        step();
    }

    public void render(final Transform transform, final Camera camera) {
        if (sprites == null || sprites.isEmpty()) return;
        if (BatchRenderer.USE_BATCH) {
            mesh.renderBatch(transform, camera);
        } else {
            mesh.render(transform, camera);
        }
    }

    public void step() {
        if (currentAnimation == null) return;
        final Animation animation = animations.get(currentAnimation);
        if (animation == null) return;
        final Sprite[] sprites = animation.getSprites();
        currentIndex++;
        if (currentIndex < 0 || currentIndex >= sprites.length) {
            currentIndex = 0;
        }

        final Vector2f[] coords = sprites[currentIndex].getTexCoords();
        for (int i = 0; i < 4; i++) {
            mesh.getVertex(i).texCoords.x = coords[i].x;
            mesh.getVertex(i).texCoords.y = coords[i].y;
        }

        mesh.setDirty(true);
    }

    public Map<String, Animation> getAnimations() {
        return animations;
    }

    public void invalidate() {
        animations.clear();
        sprites.clear();
        if (mesh.getTexture() != null) {
            mesh.getTexture().invalidate();
        }
        mesh.resetArrays();
        mesh.resetIndices();
    }

    @Override
    public Spritesheet copy() {
        final Spritesheet spritesheet = new Spritesheet(path, columns, rows);
        spritesheet.mesh = mesh.copy();
        spritesheet.mesh.setTexture(path);
        spritesheet.mesh.build();
        spritesheet.build();
        spritesheet.currentAnimation = currentAnimation;
        spritesheet.currentIndex = currentIndex;
        return spritesheet;
    }
}
