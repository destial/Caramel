package caramel.api;

import java.util.List;

public interface JoystickListener {

    void joystickCallback(int jid, int event);

    float getAxis(int joystickId, int code);

    float getRightTrigger(int joystickId);

    float getLeftTrigger(int joystickId);

    boolean isButtonPressed(int joystickId, int code);

    boolean isButtonDown(int joystickId, int code);

    boolean isButtonReleased(int joystickId, int code);

    boolean isConnected(int joystickId);

    List<Integer> getConnectedJoysticks();

    int getConnectedJoystick();

    int getDisconnectedJoystick();
}
