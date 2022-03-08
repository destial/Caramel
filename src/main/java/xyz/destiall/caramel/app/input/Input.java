package xyz.destiall.caramel.app.input;

import xyz.destiall.caramel.app.Application;

public class Input {
    public static boolean isKeyDown(int key) {
        return Application.getApp().getKeyListener().isKeyDown(key);
    }

    public static boolean isMouseDown(int mouse) {
        return Application.getApp().getMouseListener().isButtonDown(mouse);
    }

    public static float getMouseDeltaX() {
        return Application.getApp().getMouseListener().getDeltaX();
    }

    public static float getMouseDeltaY() {
        return Application.getApp().getMouseListener().getDeltaY();
    }

    public static float getMouseScroll() {
        return Application.getApp().getMouseListener().getScrollY();
    }
}
