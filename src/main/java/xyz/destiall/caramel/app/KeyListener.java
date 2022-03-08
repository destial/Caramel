package xyz.destiall.caramel.app;

import static org.lwjgl.glfw.GLFW.*;

public class KeyListener {
    private boolean[] keys = new boolean[350];
    protected KeyListener() {}

    public void keyCallback(long window, int key, int scan, int action, int mods) {
        if (action == GLFW_PRESS) {
            keys[key] = true;
        } else if (action == GLFW_RELEASE) {
            keys[key] = false;
        }
    }

    public boolean isKeyDown(int key) {
        return keys[key];
    }

    public void endFrame() {
        // Arrays.fill(keys, false);
    }
}
