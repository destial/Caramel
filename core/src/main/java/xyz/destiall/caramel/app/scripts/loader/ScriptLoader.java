package xyz.destiall.caramel.app.scripts.loader;

import xyz.destiall.caramel.api.scripts.InternalScript;
import xyz.destiall.caramel.api.utils.FileIO;

import javax.script.ScriptException;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ScriptLoader {
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final Map<String, ScriptClassLoader> loaders = new ConcurrentHashMap<>();
    private final JavaCompiler compiler;

    private ScriptMemoryManager scriptMemoryManager;
    private DiagnosticCollector<JavaFileObject> diagnostics;

    public ScriptLoader() {
        compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return;
        }
        diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        scriptMemoryManager = new ScriptMemoryManager(standardFileManager, getClass().getClassLoader());
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

    public InternalScript compile(File file, String code) throws ScriptException, MalformedURLException {
        if (compiler == null) {
            throw new ScriptException("You are not running on a compatible version of the Java Development Kit! You cannot use scripts!");
        }

        String fullClassName = getFullName(file, code);
        String simpleClassName = extractSimpleName(fullClassName);
        loaders.remove(fullClassName);
        removeClass(fullClassName);

        FileScriptMemoryJavaObject scriptSource = scriptMemoryManager.createSourceFileObject(null, simpleClassName, code);
        Collection<FileScriptMemoryJavaObject> otherScripts = loaders.values().stream().map(ScriptClassLoader::getSource).collect(Collectors.toList());
        otherScripts.add(scriptSource);
        JavaCompiler.CompilationTask task = compiler.getTask(null, scriptMemoryManager, diagnostics, null, null, otherScripts);

        if (!task.call()) {
            String message = "Error while compiling " + file.getPath() + ": " + diagnostics.getDiagnostics().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
            throw new ScriptException(message);
        }
        System.out.println("Successfully compiled " + file.getName());
        ScriptClassLoader loader = scriptMemoryManager.getClassLoader(this, file, fullClassName, scriptSource);
        loaders.put(fullClassName, loader);
        setClass(fullClassName, loader.script.getCompiledClass());
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

        throw new ScriptException("Error while compiling " + file.getPath() + ": Could not determine fully qualified class name");
    }

    private String extractSimpleName(String fullName) {
        int lastDotIndex = fullName.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return fullName;
        }
        return fullName.substring(lastDotIndex + 1);
    }
}
