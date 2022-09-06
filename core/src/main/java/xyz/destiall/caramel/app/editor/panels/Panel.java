package xyz.destiall.caramel.app.editor.panels;

import caramel.api.objects.SceneImpl;
import xyz.destiall.caramel.app.ApplicationImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Panel {
    protected final SceneImpl scene;
    protected final ApplicationImpl window;
    public Panel(final SceneImpl scene) {
        this.scene = scene;
        window = ApplicationImpl.getApp();
    }

    public abstract void __imguiLayer();

    private static final Map<Class<? extends Panel>, Boolean> hovered = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Panel>, Boolean> focused = new ConcurrentHashMap<>();

    public static void setPanelHovered(final Class<? extends Panel> clazz, final boolean b) {
        hovered.put(clazz, b);
    }

    public static void setPanelFocused(final Class<? extends Panel> clazz, final boolean b) {
        focused.put(clazz, b);
    }

    public static boolean isWindowHovered(final Class<? extends Panel> clazz) {
        return hovered.getOrDefault(clazz, false);
    }

    public static boolean isWindowFocused(final Class<? extends Panel> clazz) {
        return focused.getOrDefault(clazz, false);
    }

    public static void reset() {
        hovered.clear();
        focused.clear();
    }
}
