package caramel.api.texture;

import caramel.api.components.Camera;
import caramel.api.components.Transform;
import caramel.api.utils.Pair;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Spritesheet {
    private transient List<Sprite> sprites;
    private transient Map<String, Sprite[]> animations;
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
        step();
        loaded = true;
    }

    public void addAnimation(String name, int indexStart, int indexEnd) {
        int total = indexEnd - indexStart;
        Sprite[] animation = new Sprite[total];
        int j = 0;
        for (int i = indexStart; i < indexEnd; i++) {
            animation[j++] = sprites.get(i);
        }
        animations.put(name, animation);
        if (serializedAnimation == null) {
            serializedAnimation = new HashMap<>();
        }
        serializedAnimation.put(name, new Pair<>(indexStart, indexEnd));
        currentAnimation = name;
    }

    public void setCurrentAnimation(String animation) {
        this.currentAnimation = animation;
        this.currentIndex = 0;
    }

    public void render(Transform transform, Camera camera) {
        if (sprites == null || sprites.isEmpty()) return;
        mesh.render(transform, camera);
    }

    public void step() {
        if (currentAnimation == null) return;
        Sprite[] animation = animations.get(currentAnimation);
        currentIndex++;
        if (currentIndex >= animation.length) {
            currentIndex = 0;
        }

        Vector2f[] coords = animation[currentIndex].getTexCoords();

        for (int i = 0; i < 4; i++) {
            mesh.getVertex(i).texCoords.x = coords[i].x;
            mesh.getVertex(i).texCoords.y = coords[i].y;
        }

        mesh.setDirty(true);
    }
}
