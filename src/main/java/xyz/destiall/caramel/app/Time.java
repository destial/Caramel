package xyz.destiall.caramel.app;

public class Time {
    public static final float timeStarted = System.nanoTime();
    public static float deltaTime = 1 / 60f;
    public static boolean isSecond = false;
    public static float getFPS() {
        return 1 / deltaTime;
    }

    public static float getElapsedTime() {
        return (float) ((System.nanoTime() - timeStarted) * 1E-9);
    }
}
