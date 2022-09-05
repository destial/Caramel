package xyz.destiall.caramel.app.build;

import caramel.api.debug.Debug;
import caramel.api.utils.FileIO;
import xyz.destiall.caramel.app.scripts.loader.FileScriptMemoryJavaObject;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class MoveStage implements Stage {
    private final File root;
    private final File output;
    private final File outputBuild;
    private final Collection<FileScriptMemoryJavaObject> sources;

    public MoveStage(final File output, final File outputBuild, final Collection<FileScriptMemoryJavaObject> sources) {
        this.output = output;
        this.outputBuild = outputBuild;
        this.sources = sources;
        root = new File("." + File.separator);
    }

    @Override
    public Stage execute() {
        final File[] files = root.listFiles(f -> f.getName().endsWith(".class"));
        if (files == null || files.length == 0) {
            Debug.logError("Nothing to move! Skipping to ExtractStage...");
            return new ExtractStage(output, outputBuild);
        }

        for (final File file : files) {
            try {
                final File sourceFile = sources.stream().filter(s -> s.getName().endsWith(file.getName().replace(".class", ""))).findFirst().orElse(null).getOrigin();
                String read = FileIO.readData(sourceFile);
                String pack = FileIO.getPackage(read).replace(".", File.separator);
                final File packageOut = new File(output, pack + File.separator);
                packageOut.mkdirs();
                final File dst = new File(packageOut, file.getName());
                FileIO.copy(file, dst, null, true);
                FileIO.delete(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new ExtractStage(output, outputBuild);
    }
}
