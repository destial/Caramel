package xyz.destiall.caramel.app;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {
    private final boolean[] keys = new boolean[350];
    private final boolean[] keysPressedThisFrame = new boolean[350];
    private final boolean[] keysReleased = new boolean[350];
    protected KeyListener() {
        Arrays.fill(keysReleased, true);
    }

    public void keyCallback(long window, int key, int scan, int action, int mods) {
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

    public boolean isKeyDown(int key) {
        return keys[key];
    }

    public boolean isKeyPressedThisFrame(int key) {
        return keysPressedThisFrame[key];
    }

    public void endFrame() {
        Arrays.fill(keysPressedThisFrame, false);
    }
}
