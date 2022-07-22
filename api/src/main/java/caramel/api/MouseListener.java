package caramel.api;

public interface MouseListener {

    void mousePosCallback(long window, double xPos, double yPos);

    void mouseButtonCallback(long window, int button, int action, int mods);

    void mouseScrollCallback(long window, double xOffset, double yOffset);

    void endFrame();

    float getX();

    float getY();

    float getDeltaX();

    float getDeltaY();

    float getScrollX();

    float getScrollY();

    boolean isDragging();

    float getOrthoX();

    float getOrthoY();

    float getScreenX();

    float getScreenY();

    boolean isButtonDown(int button);

    boolean isButtonPressedThisFrame(int button);

    boolean isButtonReleasedThisFrame(int button);
}
