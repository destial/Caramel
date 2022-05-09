package xyz.destiall.caramel.api;

import org.joml.Vector2d;
import xyz.destiall.caramel.app.Application;

public interface Input {
    static boolean isKeyDown(int key) {
        return Application.getApp().getKeyListener().isKeyDown(key);
    }

    static boolean isMouseDown(int mouse) {
        return Application.getApp().getMouseListener().isButtonDown(mouse);
    }

    static boolean isMousePressed(int mouse) {
        return Application.getApp().getMouseListener().isButtonPressedThisFrame(mouse);
    }

    static boolean isKeyPressed(int key) {
        return Application.getApp().getKeyListener().isKeyPressedThisFrame(key);
    }

    static float getMouseDeltaX() {
        return Application.getApp().getMouseListener().getDeltaX();
    }

    static Vector2d getMousePosition() {
        return new Vector2d(Application.getApp().getMouseListener().getX(), Application.getApp().getMouseListener().getY());
    }

    static float getMouseDeltaY() {
        return Application.getApp().getMouseListener().getDeltaY();
    }

    static float getMouseScroll() {
        return Application.getApp().getMouseListener().getScrollY();
    }
}
