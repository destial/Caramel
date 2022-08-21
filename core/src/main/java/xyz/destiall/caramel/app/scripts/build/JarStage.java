package xyz.destiall.caramel.app.scripts.build;

import caramel.api.debug.Debug;
import caramel.api.utils.FileIO;

import java.io.File;

public final class JarStage implements Stage {
    private final File root;
    private final File output;

    public JarStage(File root, File output) {
        this.root = root;
        this.output = output;
    }

    @Override
    public Stage execute() {
        return windows();
    }

    private Stage windows() {
        try {
            String output = FileIO.asRelative(this.output);
            String directory = FileIO.asRelative(root);
            String manifestFile = FileIO.asRelative(new File(root, "META-INF" + File.separator + "MANIFEST.MF"));
            directory = directory.substring(0, directory.length() - 1);
            Debug.log("Packaging " + directory + " to " + output);
            String command = "jar cfm " + output + " " +  manifestFile + " -C " + directory + " .";
            Debug.console("Executing " + command);

            Runtime.getRuntime().exec(command).waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CleanStage(root);
    }
}
