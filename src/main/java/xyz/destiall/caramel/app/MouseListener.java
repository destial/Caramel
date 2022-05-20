package xyz.destiall.caramel.app;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import xyz.destiall.caramel.editor.EditorCamera;
import xyz.destiall.caramel.editor.ui.GamePanel;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {
    private final boolean[] mouseButtonPressed = new boolean[GLFW_MOUSE_BUTTON_LAST];
    private final boolean[] mouseButtonPressedThisFrame = new boolean[GLFW_MOUSE_BUTTON_LAST];
    private final boolean[] mouseButtonReleased = new boolean[GLFW_MOUSE_BUTTON_LAST];
    private final Vector2f gameViewportPos = new Vector2f();
    private final Vector2f gameViewportSize = new Vector2f();
    private double scrollX, scrollY, lastScrollX, lastScrollY;
    private double xPos, yPos, lastY, lastX;
    private boolean isDragging;

    private boolean firstFrame = true;

    protected MouseListener() {
        scrollX = 0;
        scrollY = 0;
        xPos = 0;
        yPos = 0;
        lastX = 0;
        lastY = 0;
        lastScrollX = 0;
        lastScrollY = 0;
        isDragging = false;
        Arrays.fill(mouseButtonReleased, true);
    }

    public void mousePosCallback(long window, double xPos, double yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        if (firstFrame) {
            lastX = xPos;
            lastY = yPos;
            return;
        }
        GamePanel panel = Application.getApp().getCurrentScene().getEditorPanel(GamePanel.class);
        if (panel != null) {
            if (xPos > panel.getGameWindowPos().x &&
                    yPos > panel.getGameWindowPos().y &&
                    xPos < panel.getGameWindowPos().x + panel.getGameWindowSize().x &&
                    yPos < panel.getGameWindowPos().y + panel.getGameWindowSize().y) {
            } else {
                Arrays.fill(mouseButtonPressed, false);
            }
        }

        isDragging = mouseButtonPressed[GLFW_MOUSE_BUTTON_1];
    }

    public void mouseButtonCallback(long window, int button, int action, int mods) {
        GamePanel panel = Application.getApp().getCurrentScene().getEditorPanel(GamePanel.class);
        if (panel != null) {
            if (xPos > panel.getGameWindowPos().x &&
                    yPos > panel.getGameWindowPos().y &&
                    xPos < panel.getGameWindowPos().x + panel.getGameWindowSize().x &&
                    yPos < panel.getGameWindowPos().y + panel.getGameWindowSize().y) {
                if (action == GLFW_PRESS) {
                    mouseButtonPressed[button] = true;
                    if (mouseButtonReleased[button]) {
                        mouseButtonPressedThisFrame[button] = true;
                        mouseButtonReleased[button] = false;
                    }
                } else if (action == GLFW_RELEASE) {
                    mouseButtonPressed[button] = false;
                    mouseButtonReleased[button] = true;
                    isDragging = false;
                }
            }
        }
    }

    public void mouseScrollCallback(long window, double xOffset, double yOffset) {
        scrollX = xOffset;
        scrollY = yOffset;
    }

    public void endFrame() {
        scrollX = 0;
        scrollY = 0;
        lastX = xPos;
        lastY = yPos;
        lastScrollX = scrollX;
        lastScrollY = scrollY;
        if (firstFrame) firstFrame = false;
        Arrays.fill(mouseButtonPressedThisFrame, false);
    }

    public float getX() {
        return (float) xPos;
    }

    public float getY() {
        return (float) yPos;
    }

    public float getDeltaX() {
        if (firstFrame) return 0;
        return (float) (xPos - lastX);
    }

    public float getDeltaY() {
        if (firstFrame) return 0;
        return (float) (yPos - lastY);
    }

    public float getScrollX() {
        return (float) (scrollX - lastScrollX);
    }

    public float getScrollY() {
        return (float) (scrollY - lastScrollY);
    }

    public boolean isDragging() {
        return isDragging;
    }

    public float getOrthoX() {
        float currentX = getX() - gameViewportPos.x;
        currentX = (currentX / gameViewportSize.x) * 2.0f - 1.0f;
        Vector4f tmp = new Vector4f(currentX, 0, 0, 1);

        EditorCamera camera = Application.getApp().getCurrentScene().getEditorCamera();
        Matrix4f viewProjection = new Matrix4f();
        camera.getInverseView().mul(camera.getInverseProjection(), viewProjection);
        tmp.mul(viewProjection);
        currentX = tmp.x;

        return currentX;
    }

    public float getOrthoY() {
        float currentY = getY() - gameViewportPos.y;
        currentY = -((currentY / gameViewportSize.y) * 2.0f - 1.0f);
        Vector4f tmp = new Vector4f(0, currentY, 0, 1);

        EditorCamera camera = Application.getApp().getCurrentScene().getEditorCamera();
        Matrix4f viewProjection = new Matrix4f();
        camera.getInverseView().mul(camera.getInverseProjection(), viewProjection);
        tmp.mul(viewProjection);
        currentY = tmp.y;

        return currentY;
    }

    public boolean isButtonDown(int button) {
        return mouseButtonPressed[button];
    }

    public boolean isButtonPressedThisFrame(int button) {
        return mouseButtonPressedThisFrame[button];
    }

    public void setGameViewportPos(Vector2f vector2f) {
        gameViewportPos.set(vector2f);
    }

    public void setGameViewportSize(Vector2f vector2f) {
        gameViewportSize.set(vector2f);
    }
}
