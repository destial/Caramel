package caramel.api;

import caramel.api.components.Camera;

import java.lang.reflect.Field;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public interface Input {

    static boolean isMouseDown(final int mouse) {
        return Application.getApp().getMouseListener().isButtonDown(mouse);
    }

    static boolean isMousePressed(final int mouse) {
        return Application.getApp().getMouseListener().isButtonPressedThisFrame(mouse);
    }

    static boolean isMouseReleased(final int mouse) {
        return Application.getApp().getMouseListener().isButtonReleasedThisFrame(mouse);
    }

    static boolean isControlPressedAnd(final int key) {
        return Input.isKeyDown(Key.CONTROL) && Input.isKeyPressed(key);
    }

    static boolean isAltPressedAnd(final int key) {
        return Input.isKeyDown(Key.ALT) && Input.isKeyPressed(key);
    }

    static boolean isCtrlAltPressedAnd(final int key) {
        return Input.isKeyDown(Key.CONTROL) && Input.isKeyDown(Key.ALT) && Input.isKeyPressed(key);
    }

    static boolean isKeyDown(final int key) {
        if (key == Key.ALT) {
            return Input.isKeyDown(Key.L_ALT) || Input.isKeyDown(Key.R_ALT);
        }
        if (key == Key.CONTROL) {
            return Input.isKeyDown(Key.L_CONTROL) || Input.isKeyDown(Key.R_CONTROL);
        }
        return Application.getApp().getKeyListener().isKeyDown(key);
    }

    static boolean isKeyPressed(final int key) {
        if (key == Key.ALT) {
            return Input.isKeyPressed(Key.L_ALT) || Input.isKeyPressed(Key.R_ALT);
        }
        if (key == Key.CONTROL) {
            return Input.isKeyPressed(Key.L_CONTROL) || Input.isKeyPressed(Key.R_CONTROL);
        }
        return Application.getApp().getKeyListener().isKeyPressedThisFrame(key);
    }

    static List<Integer> getKeysPressed() {
        return Application.getApp().getKeyListener().getKeysPressed();
    }

    static List<Integer> getKeysPressedThisFrame() {
        return Application.getApp().getKeyListener().getKeysPressedThisFrame();
    }

    static float getMouseDeltaX() {
        return Application.getApp().getMouseListener().getDeltaX();
    }

    static float getMouseScreenX() {
        return Application.getApp().getMouseListener().getX();
    }

    static float getMouseScreenY() {
        return Application.getApp().getMouseListener().getY();
    }

    static float getMouseDeltaY() {
        return Application.getApp().getMouseListener().getDeltaY();
    }

    static float getMouseScroll() {
        return Application.getApp().getMouseListener().getScrollY();
    }

    static float getMouseWorldX() {
        return Application.getApp().getMouseListener().getScreenX();
    }

    static float getMouseWorldX(final Camera camera) {
        return Application.getApp().getMouseListener().getScreenX(camera);
    }

    static float getMouseWorldY(final Camera camera) {
        return Application.getApp().getMouseListener().getScreenY(camera);
    }

    static float getMouseWorldY() {
        return Application.getApp().getMouseListener().getScreenY();
    }

    static float getJoystickAxis(final int code) {
        return Application.getApp().getJoystickListener().getAxis(Joystick.PAD1, code);
    }

    static boolean isJoystickDown(final int code) {
        return Application.getApp().getJoystickListener().isButtonDown(Joystick.PAD1, code);
    }

    static boolean isJoystickReleased(final int code) {
        return Application.getApp().getJoystickListener().isButtonReleased(Joystick.PAD1, code);
    }

    static boolean isJoystickPressed(final int code) {
        return Application.getApp().getJoystickListener().isButtonPressed(Joystick.PAD1, code);
    }

    static List<Integer> getConnectedJoysticks() {
        return Application.getApp().getJoystickListener().getConnectedJoysticks();
    }

    interface Mouse {
        int LEFT = GLFW_MOUSE_BUTTON_LEFT;
        int RIGHT = GLFW_MOUSE_BUTTON_RIGHT;
        int MIDDLE = GLFW_MOUSE_BUTTON_MIDDLE;
        int LAST = GLFW_MOUSE_BUTTON_LAST;
        int B1 = GLFW_MOUSE_BUTTON_1;
        int B2 = GLFW_MOUSE_BUTTON_2;
        int B3 = GLFW_MOUSE_BUTTON_3;
        int B4 = GLFW_MOUSE_BUTTON_4;
        int B5 = GLFW_MOUSE_BUTTON_5;
        int B6 = GLFW_MOUSE_BUTTON_6;
        int B7 = GLFW_MOUSE_BUTTON_7;
        int B8 = GLFW_MOUSE_BUTTON_8;

        static String getButtonName(final int code) {
            for (final Field field : Mouse.class.getFields()) {
                try {
                    if ((int) field.get(null) == code) {
                        return field.getName();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return "UNKNOWN";
        }
    }

    interface Joystick {
        int PAD1 = GLFW_JOYSTICK_1;
        int PAD2 = GLFW_JOYSTICK_2;
        int PAD3 = GLFW_JOYSTICK_3;
        int PAD4 = GLFW_JOYSTICK_4;
        int PAD5 = GLFW_JOYSTICK_5;
        int PAD6 = GLFW_JOYSTICK_6;
        int PAD7 = GLFW_JOYSTICK_7;
        int PAD8 = GLFW_JOYSTICK_8;
        int PAD9 = GLFW_JOYSTICK_9;
        int PAD10 = GLFW_JOYSTICK_10;
        int PAD11 = GLFW_JOYSTICK_11;
        int PAD12 = GLFW_JOYSTICK_12;
        int PAD13 = GLFW_JOYSTICK_13;
        int PAD14 = GLFW_JOYSTICK_14;
        int PAD15 = GLFW_JOYSTICK_15;
        int PAD16 = GLFW_JOYSTICK_16;

        interface Axis {
            int LEFT_X = GLFW_GAMEPAD_AXIS_LEFT_X;
            int RIGHT_X = GLFW_GAMEPAD_AXIS_RIGHT_X;

            int LEFT_Y = GLFW_GAMEPAD_AXIS_LEFT_Y;
            int RIGHT_Y = GLFW_GAMEPAD_AXIS_RIGHT_Y;

            int RIGHT_TRIGGER = GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER;
            int LEFT_TRIGGER = GLFW_GAMEPAD_AXIS_LEFT_TRIGGER;
        }

        interface Button {
            int A = GLFW_GAMEPAD_BUTTON_A;
            int B = GLFW_GAMEPAD_BUTTON_B;
            int X = GLFW_GAMEPAD_BUTTON_X;
            int Y = GLFW_GAMEPAD_BUTTON_Y;

            int CIRCLE = GLFW_GAMEPAD_BUTTON_CIRCLE;
            int CROSS = GLFW_GAMEPAD_BUTTON_CROSS;
            int SQUARE = GLFW_GAMEPAD_BUTTON_SQUARE;
            int TRIANGLE = GLFW_GAMEPAD_BUTTON_TRIANGLE;

            int LEFT_PAD = GLFW_GAMEPAD_BUTTON_DPAD_LEFT;
            int RIGHT = GLFW_GAMEPAD_BUTTON_DPAD_RIGHT;
            int UP_PAD = GLFW_GAMEPAD_BUTTON_DPAD_UP;
            int DOWN_PAD = GLFW_GAMEPAD_BUTTON_DPAD_DOWN;

            int RIGHT_BUMPER = GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER;
            int LEFT_BUMPER = GLFW_GAMEPAD_BUTTON_LEFT_BUMPER;

            int RIGHT_THUMB = GLFW_GAMEPAD_BUTTON_RIGHT_THUMB;
            int LEFT_THUMB = GLFW_GAMEPAD_BUTTON_LEFT_THUMB;

            int START = GLFW_GAMEPAD_BUTTON_START;
            int BACK = GLFW_GAMEPAD_BUTTON_BACK;
            int GUIDE = GLFW_GAMEPAD_BUTTON_GUIDE;
            int LAST = GLFW_GAMEPAD_BUTTON_LAST;
        }

        static String getPadName(final int code) {
            for (final Field field : Joystick.class.getFields()) {
                try {
                    if ((int) field.get(null) == code) {
                        return field.getName();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return "UNKNOWN";
        }
    }

    interface Key {
        int A = GLFW_KEY_A;
        int B = GLFW_KEY_B;
        int C = GLFW_KEY_C;
        int D = GLFW_KEY_D;
        int E = GLFW_KEY_E;
        int F = GLFW_KEY_F;
        int G = GLFW_KEY_G;
        int H = GLFW_KEY_H;
        int I = GLFW_KEY_I;
        int J = GLFW_KEY_J;
        int K = GLFW_KEY_K;
        int L = GLFW_KEY_L;
        int M = GLFW_KEY_M;
        int N = GLFW_KEY_N;
        int O = GLFW_KEY_O;
        int P = GLFW_KEY_P;
        int Q = GLFW_KEY_Q;
        int R = GLFW_KEY_R;
        int S = GLFW_KEY_S;
        int T = GLFW_KEY_T;
        int U = GLFW_KEY_U;
        int V = GLFW_KEY_V;
        int W = GLFW_KEY_W;
        int X = GLFW_KEY_X;
        int Y = GLFW_KEY_Y;
        int Z = GLFW_KEY_Z;

        int SPACE = GLFW_KEY_SPACE;
        int ENTER = GLFW_KEY_ENTER;
        int BACKSPACE = GLFW_KEY_BACKSPACE;
        int ESCAPE = GLFW_KEY_ESCAPE;
        int TAB = GLFW_KEY_TAB;

        int GRAVE = GLFW_KEY_GRAVE_ACCENT;
        int EQUAL = GLFW_KEY_EQUAL;
        int MINUS = GLFW_KEY_MINUS;
        int APOSTROPHE = GLFW_KEY_APOSTROPHE;
        int COMMA = GLFW_KEY_COMMA;
        int PERIOD = GLFW_KEY_PERIOD;
        int SLASH = GLFW_KEY_SLASH;
        int BACKSLASH = GLFW_KEY_BACKSLASH;
        int SEMICOLON = GLFW_KEY_SEMICOLON;
        int L_BRACKET = GLFW_KEY_LEFT_BRACKET;
        int R_BRACKET = GLFW_KEY_RIGHT_BRACKET;
        int L_ARROW = GLFW_KEY_LEFT_SUPER;
        int R_ARROW = GLFW_KEY_RIGHT_SUPER;
        int WORLD_1 = GLFW_KEY_WORLD_1;
        int WORLD_2 = GLFW_KEY_WORLD_2;

        int N0 = GLFW_KEY_0;
        int N1 = GLFW_KEY_1;
        int N2 = GLFW_KEY_2;
        int N3 = GLFW_KEY_3;
        int N4 = GLFW_KEY_4;
        int N5 = GLFW_KEY_5;
        int N6 = GLFW_KEY_6;
        int N7 = GLFW_KEY_7;
        int N8 = GLFW_KEY_8;
        int N9 = GLFW_KEY_9;

        int KP0 = GLFW_KEY_KP_0;
        int KP1 = GLFW_KEY_KP_1;
        int KP2 = GLFW_KEY_KP_2;
        int KP3 = GLFW_KEY_KP_3;
        int KP4 = GLFW_KEY_KP_4;
        int KP5 = GLFW_KEY_KP_5;
        int KP6 = GLFW_KEY_KP_6;
        int KP7 = GLFW_KEY_KP_7;
        int KP8 = GLFW_KEY_KP_8;
        int KP9 = GLFW_KEY_KP_9;
        int KP_PLUS = GLFW_KEY_KP_ADD;
        int KP_DECIMAL = GLFW_KEY_KP_DECIMAL;

        int F1 = GLFW_KEY_F1;
        int F2 = GLFW_KEY_F2;
        int F3 = GLFW_KEY_F3;
        int F4 = GLFW_KEY_F4;
        int F5 = GLFW_KEY_F5;
        int F6 = GLFW_KEY_F6;
        int F7 = GLFW_KEY_F7;
        int F8 = GLFW_KEY_F8;
        int F9 = GLFW_KEY_F9;
        int F10 = GLFW_KEY_F10;
        int F11 = GLFW_KEY_F11;
        int F12 = GLFW_KEY_F12;
        int F13 = GLFW_KEY_F13;
        int F14 = GLFW_KEY_F14;
        int F15 = GLFW_KEY_F15;
        int F16 = GLFW_KEY_F16;
        int F17 = GLFW_KEY_F17;
        int F18 = GLFW_KEY_F18;
        int F19 = GLFW_KEY_F19;
        int F20 = GLFW_KEY_F20;

        int L_SHIFT = GLFW_KEY_LEFT_SHIFT;
        int R_SHIFT = GLFW_KEY_RIGHT_SHIFT;
        int L_ALT = GLFW_KEY_LEFT_ALT;
        int R_ALT = GLFW_KEY_RIGHT_ALT;
        int ALT = L_ALT + R_ALT;
        int L_CONTROL = GLFW_KEY_LEFT_CONTROL;
        int R_CONTROL = GLFW_KEY_RIGHT_CONTROL;
        int CONTROL = L_CONTROL + R_CONTROL;
        int PAGE_UP = GLFW_KEY_PAGE_UP;
        int PAGE_DOWN = GLFW_KEY_PAGE_DOWN;
        int HOME = GLFW_KEY_HOME;
        int END = GLFW_KEY_END;
        int INSERT = GLFW_KEY_INSERT;
        int DELETE = GLFW_KEY_DELETE;
        int PAUSE = GLFW_KEY_PAUSE;
        int PRINT_SCREEN = GLFW_KEY_PRINT_SCREEN;
        int SCROLL_LOCK = GLFW_KEY_SCROLL_LOCK;
        int NUM_LOCK = GLFW_KEY_NUM_LOCK;
        int CAPS_LOCK = GLFW_KEY_CAPS_LOCK;

        int LEFT = GLFW_KEY_LEFT;
        int RIGHT = GLFW_KEY_RIGHT;
        int UP = GLFW_KEY_UP;
        int DOWN = GLFW_KEY_DOWN;

        int MENU = GLFW_KEY_MENU;
        int UNKNOWN = GLFW_KEY_UNKNOWN;

        static String getKeyName(final int code) {
            for (final Field field : Key.class.getFields()) {
                try {
                    if ((int) field.get(null) == code) {
                        return field.getName();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return "UNKNOWN";
        }
    }
}
