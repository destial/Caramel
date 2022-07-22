package caramel.api;

import java.awt.GraphicsEnvironment;

public abstract class Time {
    public static float timeStarted;
    public static float deltaTime = 1f / 60f;
    public static final float minDeltaTime = 1f / GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate();
    public static boolean isSecond = false;
    public static float getFPS() {
        return 1f / deltaTime;
    }
}
