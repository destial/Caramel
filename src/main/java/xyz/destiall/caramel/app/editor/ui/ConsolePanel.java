package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import xyz.destiall.caramel.app.editor.Scene;
import xyz.destiall.caramel.app.utils.Pair;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class ConsolePanel extends Panel {
    private static final int LOG_LIMIT = 50;
    public static final CopyOnWriteArrayList<Pair<String, Level>> LOGS = new CopyOnWriteArrayList<>();

    public ConsolePanel(Scene scene) {
        super(scene);
    }

    public static void addLog(String log) {
        String[] split = log.split("\n");
        for (String s : split) {
            LOGS.add(new Pair<>(s, Level.INFO));
            while (LOGS.size() >= LOG_LIMIT) {
                LOGS.remove(0);
            }
        }
    }

    public static void addWarning(String log) {
        String[] split = log.split("\n");
        for (String s : split) {
            LOGS.add(new Pair<>(s, Level.WARNING));
            while (LOGS.size() >= LOG_LIMIT) {
                LOGS.remove(0);
            }
        }
    }

    public static void addError(String log) {
        String[] split = log.split("\n");
        for (String s : split) {
            LOGS.add(new Pair<>(s, Level.SEVERE));
            while (LOGS.size() >= LOG_LIMIT) {
                LOGS.remove(0);
            }
        }
    }

    @Override
    public void imguiLayer() {
        ImGui.begin("Console");
        Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
        Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
        for (Pair<String, Level> log : LOGS) {
            ImGui.textColored(getColor(log.getValue()), log.getKey());
        }
        ImGui.end();

    }

    private int getColor(Level level) {
        if (level.equals(Level.INFO)) {
            return Color.WHITE.getRGB();
        } else if (level.equals(Level.WARNING)) {
            return Color.ORANGE.getRGB();
        } else if (level.equals(Level.SEVERE)) {
            return Color.RED.getRGB();
        } else {
            return Color.YELLOW.getRGB();
        }
    }
}
