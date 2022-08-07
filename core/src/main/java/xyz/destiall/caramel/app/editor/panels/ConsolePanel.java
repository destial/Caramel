package xyz.destiall.caramel.app.editor.panels;

import caramel.api.objects.SceneImpl;
import caramel.api.utils.Pair;
import imgui.ImGui;

import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Level;

public final class ConsolePanel extends Panel {
    private static final int LOG_LIMIT = 50;
    public static final ArrayList<Pair<String, Level>> LOGS = new ArrayList<>();

    public ConsolePanel(SceneImpl scene) {
        super(scene);
    }

    public static void addLog(String log) {
        String[] split = log.split("\n");
        for (String s : split) {
            if (LOGS.size() > 0) {
                Pair<String, Level> previous = LOGS.get(LOGS.size() - 1);
                if (previous != null && previous.getKey().equals(s) && previous.getValue() == Level.INFO) {
                    LOGS.remove(LOGS.size() - 1);
                }
            }
            LOGS.add(new Pair<>(s, Level.INFO));
            while (LOGS.size() >= LOG_LIMIT) {
                LOGS.remove(0);
            }
        }
    }

    public static void addWarning(String log) {
        String[] split = log.split("\n");
        for (String s : split) {
            if (LOGS.size() > 0) {
                Pair<String, Level> previous = LOGS.get(LOGS.size() - 1);
                if (previous != null && previous.getKey().equals(s) && previous.getValue() == Level.INFO) {
                    LOGS.remove(previous);
                }
            }
            LOGS.add(new Pair<>(s, Level.WARNING));
            while (LOGS.size() >= LOG_LIMIT) {
                LOGS.remove(0);
            }
        }
    }

    public static void addError(String log) {
        String[] split = log.split("\n");
        for (String s : split) {
            if (LOGS.size() > 0) {
                Pair<String, Level> previous = LOGS.get(LOGS.size() - 1);
                if (previous != null && previous.getKey().equals(s) && previous.getValue() == Level.INFO) {
                    LOGS.remove(previous);
                }
            }
            LOGS.add(new Pair<>(s, Level.SEVERE));
            while (LOGS.size() >= LOG_LIMIT) {
                LOGS.remove(0);
            }
        }
    }

    @Override
    public void __imguiLayer() {
        if (ImGui.begin("Console")) {
            Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
            Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
            for (int i = 0; i < LOGS.size(); i++) {
                Pair<String, Level> log = LOGS.get(i);
                ImGui.textColored(getColor(log.getValue()), log.getKey());
            }
        }
        ImGui.end();
    }

    private int getColor(Level level) {
        if (level.equals(Level.INFO)) {
            return Color.WHITE.getRGB();
        } else if (level.equals(Level.WARNING)) {
            return Color.RED.getRGB();
        } else if (level.equals(Level.SEVERE)) {
            return Color.BLUE.getRGB();
        } else {
            return Color.GREEN.getRGB();
        }
    }
}
