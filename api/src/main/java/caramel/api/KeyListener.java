package caramel.api;

import java.util.List;

public interface KeyListener {

    void keyCallback(long window, int key, int scan, int action, int mods);

    boolean isKeyDown(int key);

    boolean isKeyPressedThisFrame(int key);

    List<Integer> getKeysPressed();

    List<Integer> getKeysPressedThisFrame();

    void endFrame();
}
