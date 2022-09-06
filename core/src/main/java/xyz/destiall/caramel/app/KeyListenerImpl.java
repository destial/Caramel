package xyz.destiall.caramel.app;

import caramel.api.input.KeyListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public final class KeyListenerImpl implements KeyListener {
    private final boolean[] keys = new boolean[350];
    private final boolean[] keysPressedThisFrame = new boolean[350];
    private final boolean[] keysReleased = new boolean[350];
    KeyListenerImpl() {
        Arrays.fill(keysReleased, true);
    }

    public void keyCallback(final long window, final int key, final int scan, final int action, final int mods) {
        if (action == GLFW_PRESS) {
            keys[key] = true;
            if (keysReleased[key]) {
                keysPressedThisFrame[key] = true;
            }
        } else if (action == GLFW_RELEASE) {
            keys[key] = false;
            keysPressedThisFrame[key] = false;
        }
    }

    public boolean isKeyDown(final int key) {
        return keys[key];
    }

    public boolean isKeyPressedThisFrame(final int key) {
        return keysPressedThisFrame[key];
    }

    public List<Integer> getKeysPressed() {
        final List<Integer> keysPressed = new ArrayList<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            if (keys[i]) keysPressed.add(i);
        }
        return keysPressed;
    }

    public List<Integer> getKeysPressedThisFrame() {
        final List<Integer> keysPressed = new ArrayList<>(keysPressedThisFrame.length);
        for (int i = 0; i < keysPressedThisFrame.length; i++) {
            if (keysPressedThisFrame[i]) keysPressed.add(i);
        }
        return keysPressed;
    }

    public void endFrame() {
        Arrays.fill(keysPressedThisFrame, false);
    }
}
