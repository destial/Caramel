package xyz.destiall.caramel.app.editor.ui;

import xyz.destiall.caramel.app.editor.SceneImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Panel {
    protected final SceneImpl scene;
    public Panel(SceneImpl scene) {
        this.scene = scene;
    }

    public abstract void __imguiLayer();

    private static final Map<Class<? extends Panel>, Boolean> hovered = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Panel>, Boolean> focused = new ConcurrentHashMap<>();

    public static void setPanelHovered(Class<? extends Panel> clazz, boolean b) {
        hovered.put(clazz, b);
    }

    public static void setPanelFocused(Class<? extends Panel> clazz, boolean b) {
        focused.put(clazz, b);
    }

    public static boolean isWindowHovered(Class<? extends Panel> clazz) {
        return hovered.getOrDefault(clazz, false);
    }

    public static boolean isWindowFocused(Class<? extends Panel> clazz) {
        return focused.getOrDefault(clazz, false);
    }

    public static void reset() {
        hovered.clear();
        focused.clear();
    }
}
