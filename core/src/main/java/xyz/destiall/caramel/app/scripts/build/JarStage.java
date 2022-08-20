package xyz.destiall.caramel.app.scripts.build;

import caramel.api.debug.Debug;

import java.io.File;
import java.util.Arrays;

public final class JarStage implements Stage {
    private final File root;
    public JarStage(File root) {
        this.root = root;
    }

    @Override
    public Stage execute() {
        File[] files = root.listFiles(f -> f.getName().endsWith(".class"));
        if (files == null || files.length == 0) {
            Debug.logError("Nothing to jar!");
            return null;
        }

        Debug.console("Class files: " + Arrays.toString(files));
        return null;
    }
}
