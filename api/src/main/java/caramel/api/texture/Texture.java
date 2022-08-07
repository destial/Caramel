package caramel.api.texture;

import caramel.api.debug.Debug;
import org.lwjgl.BufferUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public final class Texture {
    private transient boolean loaded = false;
    private transient ByteBuffer buffer;
    private transient int width, height;
    private transient int texId;
    private String path;

    public Texture(int width, int height) {
        this.width = width;
        this.height = height;
        loaded = true;
    }

    public Texture(int width, int height, ByteBuffer buffer) {
        this.width = width;
        this.height = height;
        this.buffer = buffer;
    }

    public void invalidate() {
        glDeleteTextures(getTexId());

        if (path != null) {
            TEXTURES.remove(path);
        }
    }

    private Texture(String path) {
        this.path = path;
    }

    public void buildEmpty() {
        texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, 0);

        loaded = true;
    }

    public void buildBuffer() {
        texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        loaded = true;
    }

    public boolean buildTexture() {
        if (texId != 0 && !loaded) return false;

        File file = new File(path);
        if (!file.exists()) {
            Debug.logError("Texture file does not exist: " + file.getPath());
            return false;
        }

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        ByteBuffer image = stbi_load(getPath(), width, height, channels, 0);

        if (image == null) {
            Debug.logError("Unable to load texture: " + getPath());
            return false;
        }

        texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        if (channels.get(0) == 3) {
            this.width = width.get(0);
            this.height = height.get(0);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, this.width, this.height, 0, GL_RGB, GL_UNSIGNED_BYTE, image);
        } else if (channels.get(0) == 4) {
            this.width = width.get(0);
            this.height = height.get(0);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        } else {
            Debug.logError("Invalid channels for texture: " + getPath());
        }
        loaded = true;

        stbi_image_free(image);
        return true;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTexId() {
        return texId;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, getTexId());
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private static final List<Texture> TEXTURES = new ArrayList<>();
    public static Texture getTexture(String path) {
        Texture texture = TEXTURES.stream().filter(t -> t.getPath().equals(path)).findFirst().orElse(null);
        if (texture == null) {
            texture = new Texture(path);
            if (texture.buildTexture()) {
                TEXTURES.add(texture);
            } else {
                texture = null;
            }
        }
        return texture;
    }

    public static List<Texture> getTextures() {
        return TEXTURES;
    }

    public static void invalidateAll() {
        for (Texture t : TEXTURES) {
            if (t.isLoaded()) {
                glDeleteTextures(t.texId);
                t.texId = 0;
            }
        }
        TEXTURES.clear();
    }
}
