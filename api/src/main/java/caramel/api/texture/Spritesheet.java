package caramel.api.texture;

import caramel.api.components.Camera;
import caramel.api.components.Transform;
import caramel.api.debug.Debug;
import caramel.api.render.Animation;
import caramel.api.utils.Pair;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Spritesheet {
    private transient List<Sprite> sprites;
    private transient Map<String, Animation> animations;
    private transient Mesh mesh;
    private transient boolean loaded = false;

    private Map<String, Pair<Integer, Integer>> serializedAnimation;
    private final String path;
    private final int columns;
    private final int rows;

    public int currentIndex = 0;
    public String currentAnimation;

    public Spritesheet(String path, int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.path = path;
        serializedAnimation = new HashMap<>();
    }

    public void build() {
        if (loaded) return;
        sprites = new ArrayList<>();
        animations = new HashMap<>();
        mesh = MeshBuilder.createQuad(1);
        mesh.build();
        mesh.setTexture(path);
        Texture texture = mesh.getTexture();
        if (texture == null) return;

        int spriteHeight = texture.getHeight() / rows;
        int spriteWidth = texture.getWidth() / columns;

        int currentX = 0;
        int currentY = 0;

        for (int i = 0; i < columns; i++) {
            float rightX = (currentX + spriteWidth) / (float) texture.getWidth();
            float topY = (currentY + spriteHeight) / (float) texture.getHeight();
            float leftX = currentX / (float) texture.getWidth();
            float bottomY = currentY / (float) texture.getHeight();

            Vector2f[] texCoords = {
                    new Vector2f(rightX, topY),
                    new Vector2f(leftX, bottomY),
                    new Vector2f(rightX, bottomY),
                    new Vector2f(leftX, topY)
            };

            Sprite sprite = new Sprite(texCoords);
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
        if (serializedAnimation != null) {
            for (Map.Entry<String, Pair<Integer, Integer>> entry : serializedAnimation.entrySet()) {
                addAnimation(entry.getKey(), entry.getValue().getKey(), entry.getValue().getValue());
            }
        }
        autoDetect();
        step();
        loaded = true;
    }

    public void autoDetect() {
        if (sprites.isEmpty() || !animations.isEmpty()) return;

        for (int i = 0; i < rows; i++) {
            int start = i * columns;
            int end = start + columns - 1;
            Animation anim = addAnimation("anim" + i, start, end);
            Debug.log("Auto detected animation " + anim);
        }
    }

    public Animation addAnimation(String name, int indexStart, int indexEnd) {
        int total = indexEnd - indexStart;
        Sprite[] animation = new Sprite[total + 1];
        int j = 0;
        for (int i = indexStart; i <= indexEnd; i++) {
            animation[j++] = sprites.get(i);
        }
        Animation anim = new Animation(animation);
        anim.name = name;
        anim.start = indexStart;
        anim.end = indexEnd;
        animations.put(name, anim);
        if (serializedAnimation == null) {
            serializedAnimation = new HashMap<>();
        }
        serializedAnimation.put(name, new Pair<>(indexStart, indexEnd));
        currentAnimation = name;
        return anim;
    }

    public void setCurrentAnimation(String animation) {
        this.currentAnimation = animation;
        this.currentIndex = -1;
        step();
    }

    public void render(Transform transform, Camera camera) {
        if (sprites == null || sprites.isEmpty()) return;
        mesh.render(transform, camera);
    }

    public void step() {
        if (currentAnimation == null) return;
        Animation animation = animations.get(currentAnimation);
        if (animation == null) return;
        Sprite[] sprites = animation.getSprites();
        currentIndex++;
        if (currentIndex >= sprites.length) {
            currentIndex = 0;
        }

        Vector2f[] coords = sprites[currentIndex].getTexCoords();

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

    public Spritesheet copy() {
        Spritesheet spritesheet = new Spritesheet(path, columns, rows);
        spritesheet.build();
        spritesheet.currentAnimation = currentAnimation;
        spritesheet.currentIndex = currentIndex;
        return spritesheet;
    }
}
