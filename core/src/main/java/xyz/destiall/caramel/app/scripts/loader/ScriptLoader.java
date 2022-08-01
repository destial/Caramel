package xyz.destiall.caramel.app.scripts.loader;

import caramel.api.debug.Debug;
import caramel.api.scripts.InternalScript;
import caramel.api.utils.FileIO;
import xyz.destiall.caramel.app.scripts.EditorScriptManager;

import javax.script.ScriptException;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        Debug.console("Searching for script " + name);
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

    void removeClass(String name) {
        classes.remove(name);
    }

    public InternalScript compile(File file, String code) throws ScriptException, MalformedURLException {
        if (compiler == null) {
            throw new ScriptException("You are not running on a compatible version of the Java Development Kit! You cannot use scripts!");
        }

        String fullClassName = getFullName(file, code);

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

        ScriptClassLoader previous = loaders.get(fullClassName);
        ScriptClassLoader loader = scriptMemoryManager.getClassLoader(this, file, fullClassName, scriptSource);
        if (previous != null) {
            removeClass(previous.getFullClassName());
            setClass(fullClassName, loader.script.getCompiledClass());
            try {
                previous.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loaders.put(fullClassName, loader);
        setClass(fullClassName, loader.script.getCompiledClass());
        Debug.console("Recompiled " + file.getName());
        return loader.script;
    }

    public InternalScript compile(File file) throws ScriptException, FileNotFoundException, MalformedURLException {
        if (!file.exists()) throw new FileNotFoundException();
        String code = FileIO.readData(file);
        return compile(file, code);
    }

    private static final Pattern NAME_PATTERN = Pattern.compile("public\\s+class\\s+([A-Za-z][A-Za-z0-9_$]*)");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([A-Za-z][A-Za-z0-9_$.]*)");

    private String getFullName(File file, String script) throws ScriptException {
        String fullPackage = null;
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(script);
        if (packageMatcher.find()) {
            fullPackage = packageMatcher.group(1);
        }

        Matcher nameMatcher = NAME_PATTERN.matcher(script);
        if (nameMatcher.find()) {
            String name = nameMatcher.group(1);
            if (fullPackage == null) {
                return name;
            } else {
                return fullPackage + "." + name;
            }
        }
        return "scripts." + file.getName().substring(0, file.getName().length() - "java".length());
    }

    private String extractSimpleName(String fullName) {
        int lastDotIndex = fullName.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return fullName;
        }
        return fullName.substring(lastDotIndex + 1);
    }

    public void compileAll(List<File> files) throws ScriptException {
        if (compiler == null) {
            throw new ScriptException("You are not running on a compatible version of the Java Development Kit! You cannot use scripts!");
        }
        List<FileScriptMemoryJavaObject> sources = new ArrayList<>(files.size());
        for (File file : files) {
            if (!file.exists()) continue;
            String code = FileIO.readData(file);
            String fullClassName = getFullName(file, code);
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
            ScriptClassLoader loader = null;
            try {
                loader = scriptMemoryManager.getClassLoader(this, source.getOrigin(), source.getName(), source);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (loader == null) continue;

            loaders.put(source.getName(), loader);
            setClass(source.getName(), loader.script.getCompiledClass());
        }
    }
}
