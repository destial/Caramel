package xyz.destiall.caramel.app.scripts.build;

import caramel.api.debug.Debug;
import caramel.api.utils.FileIO;
import xyz.destiall.caramel.app.scripts.loader.FileScriptMemoryJavaObject;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class MoveStage implements Stage {
    private final File root;
    private final File output;
    private final File outputBuild;
    private final Collection<FileScriptMemoryJavaObject> sources;

    public MoveStage(File output, File outputBuild, Collection<FileScriptMemoryJavaObject> sources) {
        this.output = output;
        this.outputBuild = outputBuild;
        this.sources = sources;
        root = new File("." + File.separator);
    }

    @Override
    public boolean isReady() {
        List<File> files = FileIO.traverse(root).stream().filter(f -> f.getName().endsWith(".class")).collect(Collectors.toList());
        Debug.log(root.getPath());
        return files.size() != 0;
    }

    @Override
    public Stage execute() {
        List<File> files = FileIO.traverse(root).stream().filter(f -> f.getName().endsWith(".class")).collect(Collectors.toList());
        if (files.size() == 0) {
            Debug.logError("Nothing to move!");
            return null;
        }

        for (File file : files) {
            try {
                File sourceFile = sources.stream().filter(s -> s.getName().endsWith(file.getName().replace(".class", ""))).findFirst().orElse(null).getOrigin();
                String read = FileIO.readData(sourceFile);
                String pack = FileIO.getPackage(read).replace(".", File.separator);
                Debug.console(pack);
                File packageOut = new File(output, pack + File.separator);
                packageOut.mkdirs();
                File dst = new File(packageOut, file.getName());
                FileIO.copy(file, dst, null, true);
                Debug.log("Moving file " + file.getPath() + " to " + dst.getPath());
                FileIO.delete(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new ExtractStage(output, outputBuild);
    }
}
