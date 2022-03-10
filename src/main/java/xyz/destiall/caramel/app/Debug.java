package xyz.destiall.caramel.app;

import xyz.destiall.caramel.editor.ui.ConsolePanel;

public class Debug {
    public static void log(Object log) {
        ConsolePanel.addToLog(""+log);
    }

    public static void logError(Object error) {
        ConsolePanel.addError("ERROR: " + error);
    }
}
