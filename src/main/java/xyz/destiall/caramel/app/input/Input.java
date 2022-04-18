package xyz.destiall.caramel.app.input;

import org.joml.Vector2d;
import xyz.destiall.caramel.app.Application;

public class Input {
    public static boolean isKeyDown(int key) {
        return Application.getApp().getKeyListener().isKeyDown(key);
    }

    public static boolean isMouseDown(int mouse) {
        return Application.getApp().getMouseListener().isButtonDown(mouse);
    }

    public static boolean isMousePressed(int mouse) {
        return Application.getApp().getMouseListener().isButtonPressedThisFrame(mouse);
    }

    public static boolean isKeyPressed(int key) {
        return Application.getApp().getKeyListener().isKeyPressedThisFrame(key);
    }

    public static float getMouseDeltaX() {
        return Application.getApp().getMouseListener().getDeltaX();
    }

    public static Vector2d getMousePosition() {
        return new Vector2d(Application.getApp().getMouseListener().getX(), Application.getApp().getMouseListener().getY());
    }

    public static float getMouseDeltaY() {
        return Application.getApp().getMouseListener().getDeltaY();
    }

    public static float getMouseScroll() {
        return Application.getApp().getMouseListener().getScrollY();
    }
}
