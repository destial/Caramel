package xyz.destiall.caramel.app.scripts.build;

import caramel.api.debug.Debug;
import caramel.api.utils.FileIO;

import java.io.File;

public final class MoveStage implements Stage {
    private final File root;
    private final File output;

    public MoveStage(File output) {
        this.output = output;
        root = new File("." + File.separator);
    }

    @Override
    public boolean isReady() {
        File[] files = root.listFiles(f -> f.getName().endsWith(".class"));
        return files != null && files.length != 0;
    }

    @Override
    public Stage execute() {
        File[] files = root.listFiles(f -> f.getName().endsWith(".class"));
        if (files == null || files.length == 0) {
            Debug.logError("Nothing to move!");
            return null;
        }

        for (File file : files) {
            try {
                FileIO.copy(file, new File(output, file.getName()), true);
                Debug.log("Moving file " + file.getName());
                FileIO.delete(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new JarStage(output);
    }
}
