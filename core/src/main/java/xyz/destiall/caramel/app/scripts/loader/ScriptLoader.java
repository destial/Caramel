package xyz.destiall.caramel.app.scripts.loader;

import caramel.api.Application;
import caramel.api.debug.Debug;
import caramel.api.scripts.InternalScript;
import caramel.api.utils.FileIO;
import xyz.destiall.caramel.app.scripts.EditorScriptManager;
import xyz.destiall.caramel.app.build.CompileStage;
import xyz.destiall.caramel.app.build.Stage;
import xyz.destiall.caramel.app.utils.Payload;

import javax.script.ScriptException;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ScriptLoader {
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final Map<String, ScriptClassLoader> loaders = new ConcurrentHashMap<>();
    private final JavaCompiler compiler;
    private EditorScriptManager scriptManager;

    private ScriptMemoryManager scriptMemoryManager;
    private DiagnosticCollector<JavaFileObject> diagnostics;

    public ScriptLoader(EditorScriptManager scriptManager) {
        compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return;
        }
        diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        scriptMemoryManager = new ScriptMemoryManager(standardFileManager, getClass().getClassLoader());
        this.scriptManager = scriptManager;
    }

    public InternalScript get(String name) {
        Map.Entry<String, ScriptClassLoader> entry = loaders.entrySet().stream().filter(en -> en.getKey().endsWith(name)).findFirst().orElse(null);
        if (entry == null) return null;
        return entry.getValue().script;
    }

    public InternalScript getByClass(Class<?> clazz) {
        return get(clazz.getName());
    }

    public InternalScript removeScript(String name) {
        Map.Entry<String, ScriptClassLoader> entry = loaders.entrySet().stream().filter(en -> en.getKey().endsWith(name)).findFirst().orElse(null);
        if (entry == null) return null;
        Debug.console("Removing script " + name);
        loaders.remove(entry.getKey());
        classes.remove(entry.getKey());
        return entry.getValue().script;
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

    Class<?> getClass(String name) {
        return classes.get(name);
    }

    void setClass(final String name, final Class<?> clazz) {
        if (!classes.containsKey(name)) {
            classes.put(name, clazz);
        }
    }

    boolean removeClass(String name) {
        return classes.remove(name) != null;
    }

    public InternalScript compile(File file, String code) throws ScriptException, MalformedURLException {
        if (compiler == null) {
            throw new NullPointerException("You are not running on a compatible version of the Java Development Kit! You cannot use scripts!");
        }

        String fullClassName = FileIO.getFullName(file, code);
        Debug.console("Building " + fullClassName);

        FileScriptMemoryJavaObject scriptSource = scriptMemoryManager.createSourceFileObject(file, fullClassName, code);
        Collection<FileScriptMemoryJavaObject> otherScripts = loaders.values().stream().filter(l -> !l.getFullClassName().equals(fullClassName)).map(ScriptClassLoader::getSource).collect(Collectors.toList());
        otherScripts.add(scriptSource);

        JavaCompiler.CompilationTask task = compiler.getTask(null, scriptMemoryManager, diagnostics, null, null, otherScripts);
        if (!task.call()) {
            String message = "Error while compiling " + file.getPath() + ": " + diagnostics.getDiagnostics().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
            throw new ScriptException(message);
        }

        for (Map.Entry<String, ScriptClassLoader> entry : loaders.entrySet()) {
            ScriptClassLoader previous = entry.getValue();
            Debug.console("Recompiling " + previous.getFile().getName());
            removeClass(previous.getFullClassName());
            ScriptClassLoader newLoader = scriptMemoryManager.getClassLoader(this, previous.getFile(), previous.getFullClassName(), previous.getSource());
            entry.setValue(newLoader);
            setClass(previous.getFullClassName(), newLoader.script.getCompiledClass());
            scriptManager.loadComponents(previous.script, newLoader.script);
            try {
                previous.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ScriptClassLoader previous = loaders.remove(fullClassName);
        ScriptClassLoader loader = scriptMemoryManager.getClassLoader(this, file, fullClassName, scriptSource);
        if (previous != null) {
            removeClass(previous.getFullClassName());
            scriptManager.loadComponents(previous.script, loader.script);
            try {
                previous.close();
                previous.clearAssertionStatus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loaders.put(fullClassName, loader);
        setClass(fullClassName, loader.script.getCompiledClass());
        Debug.console("Recompiled " + file.getName());
        return loader.script;
    }

    public void compileAll(List<File> files) throws ScriptException {
        if (compiler == null) {
            throw new NullPointerException("You are not running on a compatible version of the Java Development Kit! You cannot use scripts!");
        }
        List<FileScriptMemoryJavaObject> sources = new ArrayList<>(files.size());
        for (File file : files) {
            if (!file.exists()) continue;
            String code = FileIO.readData(file);
            String fullClassName = FileIO.getFullName(file, code);
            FileScriptMemoryJavaObject object = scriptMemoryManager.createSourceFileObject(file, fullClassName, code);
            sources.add(object);
        }

        JavaCompiler.CompilationTask task = compiler.getTask(null, scriptMemoryManager, diagnostics, null, null, sources);
        if (!task.call()) {
            String message = "Error while compiling sources: " + diagnostics.getDiagnostics().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
            throw new ScriptException(message);
        }

        for (FileScriptMemoryJavaObject source : sources) {
            Debug.console("Compiling " + source.getOrigin().getName());
            ScriptClassLoader previous = loaders.get(source.getName());
            removeClass(source.getName());
            ScriptClassLoader loader = null;
            try {
                loader = scriptMemoryManager.getClassLoader(this, source.getOrigin(), source.getName(), source);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (loader == null) continue;
            removeClass(source.getName());
            if (previous != null) {
                Payload.COMPONENTS.remove(previous.script.getCompiledClass());
                scriptManager.loadComponents(previous.script, loader.script);
                try {
                    previous.close();
                    previous.clearAssertionStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            loaders.put(source.getName(), loader);
            setClass(source.getName(), loader.script.getCompiledClass());
            Payload.COMPONENTS.add(loader.script.getCompiledClass());
        }
    }

    public void build(File root, File output) throws ScriptException {
        if (compiler == null) {
            throw new NullPointerException("You are not running on a compatible version of the Java Development Kit! You cannot use scripts!");
        }
        scriptManager.setCompileTask(Application.getApp().getScheduler().runTask(() -> {
            if (root.exists()) {
                Debug.log("Cleaning build files...");
                FileIO.delete(root);
            }
            root.mkdir();
            Stage next = new CompileStage(compiler, diagnostics, root, output, loaders.values().stream().map(ScriptClassLoader::getSource).collect(Collectors.toList()));
            while (next != null) {
                Debug.log("Starting " + next.getName());
                next = next.execute();
            }
            Debug.log("Build complete!");
            scriptManager.setCompileTask(null);
        }));
    }
}
