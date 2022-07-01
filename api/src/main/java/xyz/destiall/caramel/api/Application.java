package xyz.destiall.caramel.api;

import xyz.destiall.caramel.api.objects.Scene;
import xyz.destiall.caramel.api.scripts.ScriptManager;
import xyz.destiall.java.events.EventHandling;

import java.io.File;

public abstract class Application implements Runnable {
    protected static Application inst;

    public static Application getApp() {
        return inst;
    }

    public abstract EventHandling getEventHandler();

    public abstract void setTitle(String title);

    public abstract int getHeight();

    public abstract int getWidth();

    public abstract Scene getCurrentScene();

    public abstract Scene loadScene(File file);

    public abstract void saveCurrentScene();

    public abstract void saveScene(Scene scene, File file);

    public abstract void saveAllScenes();

    public abstract ScriptManager getScriptManager();

    public abstract MouseListener getMouseListener();

    public abstract KeyListener getKeyListener();
}
