package xyz.destiall.caramel.api;

import xyz.destiall.caramel.editor.ui.ConsolePanel;

public interface Debug {
    static void log(Object log) {
        ConsolePanel.addToLog(""+log);
    }

    static void logError(Object error) {
        ConsolePanel.addError("ERROR: " + error);
    }
}
