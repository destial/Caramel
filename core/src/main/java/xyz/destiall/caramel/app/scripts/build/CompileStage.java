package xyz.destiall.caramel.app.scripts.build;

import caramel.api.debug.Debug;
import caramel.api.utils.FileIO;
import com.google.common.collect.Lists;
import xyz.destiall.caramel.app.scripts.loader.FileScriptMemoryJavaObject;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

public final class CompileStage implements Stage {
    private final JavaCompiler compiler;
    private final Collection<FileScriptMemoryJavaObject> sources;
    private final DiagnosticCollector<JavaFileObject> diagnostics;
    private final File root;
    public CompileStage(JavaCompiler compiler, DiagnosticCollector<JavaFileObject> diagnostics, File root, Collection<FileScriptMemoryJavaObject> sources) {
        this.compiler = compiler;
        this.sources = sources;
        this.diagnostics = diagnostics;
        this.root = root;
    }

    @Override
    public Stage execute() {
        if (sources.isEmpty()) {
            Debug.logError("Nothing to compile!");
            return null;
        }
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ENGLISH, Charset.defaultCharset());
        JavaFileManager.Location loc = new JavaFileManager.Location() {
            @Override
            public String getName() {
                return root.getPath();
            }

            @Override
            public boolean isOutputLocation() {
                return true;
            }
        };
        try {
            fileManager.setLocation(loc, Collections.singletonList(root));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, Arrays.asList("-d", "./" + FileIO.asRelative(root)), null, sources);
        if (!task.call()) {
            String message = "Error while compiling sources: " + diagnostics.getDiagnostics().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
            Debug.logError(message);
            return null;
        }

        return new WaitStage(new MoveStage(root));
    }
}
