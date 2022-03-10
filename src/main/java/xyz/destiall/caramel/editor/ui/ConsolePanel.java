package xyz.destiall.caramel.editor.ui;

import imgui.ImGui;
import xyz.destiall.caramel.app.utils.Pair;
import xyz.destiall.caramel.editor.Scene;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConsolePanel extends Panel {
    public static final List<Pair<String, Level>> LOGS = new ArrayList<>();

    public ConsolePanel(Scene scene) {
        super(scene);
    }

    public static void addToLog(String log) {
        String[] split = log.split("\n");
        for (String s : split) {
            LOGS.add(new Pair<>(s, Level.INFO));
        }
    }

    public static void addWarning(String log) {
        String[] split = log.split("\n");
        for (String s : split) {
            LOGS.add(new Pair<>(s, Level.WARNING));
        }
    }

    public static void addError(String log) {
        String[] split = log.split("\n");
        for (String s : split) {
            LOGS.add(new Pair<>(s, Level.SEVERE));
        }
    }

    @Override
    public void imguiLayer() {
        ImGui.begin("Console");
        //ImGui.beginListBox("##logs");
        for (Pair<String, Level> log : LOGS) {
            ImGui.textColored(getColor(log.getValue()), log.getKey());
        }
        //ImGui.endListBox();
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
