package caramel.api;

import caramel.api.input.JoystickListener;
import caramel.api.input.KeyListener;
import caramel.api.input.MouseListener;
import caramel.api.objects.Scene;
import caramel.api.scripts.ScriptManager;
import xyz.destiall.java.events.EventHandling;
import xyz.destiall.java.timer.Scheduler;

import java.io.File;

public abstract class Application {
    protected static Application inst;

    public static Application getApp() {
        return inst;
    }

    public abstract EventHandling getEventHandler();

    public abstract Scheduler getScheduler();

    public abstract void setTitle(final String title);

    public abstract int getHeight();

    public abstract int getWidth();

    public abstract Scene getCurrentScene();

    public abstract Scene loadScene(final File file);

    public abstract Scene loadScene(final int index);

    public abstract void saveCurrentScene();

    public abstract void saveScene(final Scene scene, final File file);

    public abstract void saveAllScenes();

    public abstract boolean isRunning();

    public abstract void setRunning(final boolean run);

    public abstract ScriptManager getScriptManager();

    public abstract MouseListener getMouseListener();

    public abstract KeyListener getKeyListener();

    public abstract JoystickListener getJoystickListener();

    public abstract boolean isFullScreen();
}
