package xyz.destiall.caramel.app.scripts.loader;

import ch.obermuhlner.scriptengine.java.name.DefaultNameStrategy;
import ch.obermuhlner.scriptengine.java.name.NameStrategy;
import xyz.destiall.caramel.api.scripts.ScriptManager;
import xyz.destiall.caramel.app.utils.FileIO;

import javax.script.ScriptException;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ScriptLoader {
    private final ScriptManager scriptManager;
    private final Map<String, Class<?>> classes = new HashMap<>();
    private final Map<String, ScriptClassLoader> loaders = new LinkedHashMap<>();
    private final NameStrategy nameStrategy;
    private final ScriptMemoryManager scriptMemoryManager;
    private final JavaCompiler compiler;
    private final DiagnosticCollector<JavaFileObject> diagnostics;

    public ScriptLoader(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
        nameStrategy = new DefaultNameStrategy();

        compiler = ToolProvider.getSystemJavaCompiler();
        diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        scriptMemoryManager = new ScriptMemoryManager(standardFileManager, getClass().getClassLoader());
    }

    public ScriptManager getScriptManager() {
        return scriptManager;
    }

    Class<?> getClassByName(final String name) {
        Class<?> cachedClass = classes.get(name);

        if (cachedClass != null) {
            return cachedClass;
        } else {
            for (String current : loaders.keySet()) {
                ScriptClassLoader loader = loaders.get(current);
                try {
                    cachedClass = loader.findClass(name);
                } catch (ClassNotFoundException ignored) {}
                if (cachedClass != null) {
                    return cachedClass;
                }
            }
        }
        return null;
    }

    Iterable<String> getClasses() {
        return classes.keySet();
    }

    void setClass(final String name, final Class<?> clazz) {
        if (!classes.containsKey(name)) {
            classes.put(name, clazz);
        }
    }

    void removeClass(String name) {
        classes.remove(name);
    }

    public Script compile(File file) throws ScriptException, FileNotFoundException, MalformedURLException {
        if (!file.exists()) throw new FileNotFoundException();
        String code = FileIO.readData(file);

        String fullClassName = nameStrategy.getFullName(code);
        String simpleClassName = NameStrategy.extractSimpleName(fullClassName);

        JavaFileObject scriptSource = scriptMemoryManager.createSourceFileObject(null, simpleClassName, code);
        System.out.println(getClasses());
        JavaCompiler.CompilationTask task = compiler.getTask(null, scriptMemoryManager, diagnostics, null, null, Collections.singletonList(scriptSource));

        if (!task.call()) {
            String message = diagnostics.getDiagnostics().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
            throw new ScriptException(message);
        }
        System.out.println("Loading " + file.getName());
        ScriptClassLoader loader = scriptMemoryManager.getClassLoader(this, file, fullClassName, simpleClassName);
        loaders.put(loader.script.getName(), loader);
        setClass(fullClassName, loader.script.getCompiledClass());
        return loader.script;
    }
}
