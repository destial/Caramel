package caramel.api.texture;

import caramel.api.debug.Debug;
import caramel.api.graphics.Graphics;
import org.lwjgl.BufferUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static caramel.api.graphics.GL20.GL_LINEAR;
import static caramel.api.graphics.GL20.GL_NEAREST;
import static caramel.api.graphics.GL20.GL_REPEAT;
import static caramel.api.graphics.GL20.GL_RGB;
import static caramel.api.graphics.GL20.GL_RGBA;
import static caramel.api.graphics.GL20.GL_TEXTURE_2D;
import static caramel.api.graphics.GL20.GL_TEXTURE_MAG_FILTER;
import static caramel.api.graphics.GL20.GL_TEXTURE_MIN_FILTER;
import static caramel.api.graphics.GL20.GL_TEXTURE_WRAP_S;
import static caramel.api.graphics.GL20.GL_TEXTURE_WRAP_T;
import static caramel.api.graphics.GL20.GL_UNSIGNED_BYTE;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public final class Texture {
    private transient boolean loaded = false;
    private transient ByteBuffer buffer;
    private transient int width, height;
    private transient int texId;
    private String path;

    public Texture(final int width, final int height) {
        this.width = width;
        this.height = height;
        loaded = true;
    }

    public Texture(final int width, final int height, final ByteBuffer buffer) {
        this.width = width;
        this.height = height;
        this.buffer = buffer;
    }

    public void invalidate() {
        Graphics.get().glDeleteTextures(getTexId());

        if (path != null) {
            TEXTURES.remove(this);
        }
    }

    private Texture(final String path) {
        this.path = path;
    }

    public void buildEmpty() {
        texId = Graphics.get().glGenTextures();
        Graphics.get().glBindTexture(GL_TEXTURE_2D, texId);
        Graphics.get().glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        Graphics.get().glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        Graphics.get().glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, 0);

        loaded = true;
    }

    public void buildBuffer() {
        texId = Graphics.get().glGenTextures();
        Graphics.get(). glBindTexture(GL_TEXTURE_2D, texId);

        Graphics.get().glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        Graphics.get().glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        Graphics.get().glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        Graphics.get().glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        Graphics.get().glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        loaded = true;
    }

    public boolean buildTexture() {
        if (texId != 0 && !loaded) return false;

        final File file = new File(path);
        if (!file.exists()) {
            Debug.logError("Texture file does not exist: " + file.getPath());
            return false;
        }

        final IntBuffer width = BufferUtils.createIntBuffer(1);
        final IntBuffer height = BufferUtils.createIntBuffer(1);
        final IntBuffer channels = BufferUtils.createIntBuffer(1);
        final ByteBuffer image = stbi_load(getPath(), width, height, channels, 0);

        if (image == null) {
            Debug.logError("Unable to load texture: " + getPath());
            return false;
        }

        texId = Graphics.get().glGenTextures();
        Graphics.get().glBindTexture(GL_TEXTURE_2D, texId);

        Graphics.get().glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        Graphics.get().glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        Graphics.get().glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        Graphics.get().glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        if (channels.get(0) == 3) {
            this.width = width.get(0);
            this.height = height.get(0);
            Graphics.get().glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, this.width, this.height, 0, GL_RGB, GL_UNSIGNED_BYTE, image);
        } else if (channels.get(0) == 4) {
            this.width = width.get(0);
            this.height = height.get(0);
            Graphics.get().glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
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
        Graphics.get().glBindTexture(GL_TEXTURE_2D, getTexId());
    }

    public void unbind() {
        Graphics.get().glBindTexture(GL_TEXTURE_2D, 0);
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    private static final List<Texture> TEXTURES = new ArrayList<>();

    public static Texture getTexture(final String path) {
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
        for (final Texture t : TEXTURES) {
            if (t.isLoaded()) {
                Graphics.get().glDeleteTextures(t.texId);
                t.texId = 0;
            }
        }
        TEXTURES.clear();
    }
}
