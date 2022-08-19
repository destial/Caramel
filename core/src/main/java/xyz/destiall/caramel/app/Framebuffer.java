package xyz.destiall.caramel.app;

import caramel.api.debug.DebugImpl;
import caramel.api.graphics.Graphics;
import caramel.api.texture.Texture;

import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_DEPTH_COMPONENT32;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindRenderbuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glDeleteRenderbuffers;
import static org.lwjgl.opengl.GL30.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glGenRenderbuffers;
import static org.lwjgl.opengl.GL30.glRenderbufferStorage;

public final class Framebuffer {
    private int fboId;
    private int rboId;
    private Texture texture;
    private int width;
    private int height;

    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;

        generate();
    }

    public void generate() {
        fboId = Graphics.get().glGenFramebuffers();
        bind();
        Graphics.get().glViewport(0, 0, width, height);
        texture = new Texture(width, height);
        texture.buildEmpty();

        Graphics.get().glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getTexId(), 0);

        rboId = Graphics.get().glGenRenderbuffers();
        Graphics.get().glBindRenderbuffer(GL_RENDERBUFFER, rboId);
        Graphics.get().glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32, width, height);
        Graphics.get().glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboId);

        if (Graphics.get().glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            DebugImpl.logError("Framebuffer incomplete!");
            return;
        }

        unbind();
    }

    public void invalidate() {
        Graphics.get().glDeleteFramebuffers(fboId);
        Graphics.get().glDeleteRenderbuffers(rboId);
        fboId = 0;
        rboId = 0;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        texture.invalidate();
        invalidate();

        generate();
    }

    public void bind() {
        Graphics.get().glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    }

    public void unbind() {
        Graphics.get().glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Texture getTexture() {
        return texture;
    }

    public int getFboId() {
        return fboId;
    }
}
