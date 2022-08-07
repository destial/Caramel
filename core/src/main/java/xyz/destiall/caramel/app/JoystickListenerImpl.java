package xyz.destiall.caramel.app;

import caramel.api.Input;
import caramel.api.JoystickListener;
import org.lwjgl.glfw.GLFWGamepadState;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_CONNECTED;
import static org.lwjgl.glfw.GLFW.GLFW_DISCONNECTED;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetGamepadState;
import static org.lwjgl.glfw.GLFW.glfwJoystickIsGamepad;
import static org.lwjgl.glfw.GLFW.glfwJoystickPresent;

public final class JoystickListenerImpl implements JoystickListener {

    private final Map<Integer, GLFWGamepadState> gamepads;
    private final Map<Integer, List<Integer>> buttonPressed;
    private int connectedJoystick = -1;
    private int disconnectedJoystick = -1;

    JoystickListenerImpl() {
        gamepads = new HashMap<>();
        buttonPressed = new HashMap<>();
        for (int i = Input.Joystick.PAD1; i <= Input.Joystick.PAD16; i++) {
            gamepads.put(i, GLFWGamepadState.calloc());
            buttonPressed.put(i, new ArrayList<>());
        }
    }

    public void startFrame() {
        for (int i = Input.Joystick.PAD1; i <= Input.Joystick.PAD16; i++) {
            if (isConnected(i)) {
                glfwGetGamepadState(i, gamepads.get(i));
                ByteBuffer buttons = gamepads.get(i).buttons();
                List<Integer> buttonsPressed = buttonPressed.get(i);

                for (int b = 0; b < buttons.limit(); b++) {
                    byte state = buttons.get(b);
                    if (state == GLFW_PRESS) {
                        if (buttonsPressed.contains(b)) {
                            buttonsPressed.remove((Integer) b);
                        } else {
                            buttonsPressed.add(b);
                        }
                    } else if (state == GLFW_RELEASE) {
                        buttonsPressed.remove((Integer) b);
                    }
                }
            }
        }
    }

    @Override
    public void joystickCallback(int jid, int event) {
        if (event == GLFW_CONNECTED) {
            connectedJoystick = jid;
        } else if (event == GLFW_DISCONNECTED) {
            disconnectedJoystick = jid;
        }
    }

    @Override
    public float getAxis(int joystickId, int code) {
        return isConnected(joystickId) ? gamepads.get(joystickId).axes(code) : 0f;
    }

    @Override
    public float getRightTrigger(int joystickId) {
        return isConnected(joystickId) ? getAxis(joystickId, Input.Joystick.Axis.RIGHT_TRIGGER) : 0f;
    }

    @Override
    public float getLeftTrigger(int joystickId) {
        return isConnected(joystickId) ? getAxis(joystickId, Input.Joystick.Axis.LEFT_TRIGGER) : 0f;
    }

    @Override
    public boolean isButtonPressed(int joystickId, int code) {
        return isConnected(joystickId) && buttonPressed.get(joystickId).contains(code);
    }

    @Override
    public boolean isButtonDown(int joystickId, int code) {
        return isConnected(joystickId) && gamepads.get(joystickId).buttons(code) == GLFW_PRESS;
    }

    @Override
    public boolean isButtonReleased(int joystickId, int code) {
        return isConnected(joystickId) && gamepads.get(joystickId).buttons(code) == GLFW_RELEASE;
    }

    @Override
    public boolean isConnected(int joystick) {
        return glfwJoystickPresent(joystick) && glfwJoystickIsGamepad(joystick);
    }

    @Override
    public List<Integer> getConnectedJoysticks() {
        List<Integer> list = new ArrayList<>();
        for (int i = Input.Joystick.PAD1; i <= Input.Joystick.PAD16; i++) {
            if (isConnected(i)) {
                list.add(i);
            }
        }
        return list;
    }

    @Override
    public int getConnectedJoystick() {
        return connectedJoystick;
    }

    @Override
    public int getDisconnectedJoystick() {
        return disconnectedJoystick;
    }

    public void endFrame() {
        connectedJoystick = -1;
        disconnectedJoystick = -1;
    }
}
