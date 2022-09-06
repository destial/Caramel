package xyz.destiall.caramel.app.scripts.loader;

import caramel.api.debug.Debug;
import caramel.api.utils.CompositeIterator;

import javax.script.ScriptException;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static javax.tools.StandardLocation.CLASS_OUTPUT;

public final class ScriptMemoryManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, ClassScriptMemoryJavaObject> mapNameToClasses = new ConcurrentHashMap<>();
    private final ClassLoader parentClassLoader;

    public ScriptMemoryManager(final JavaFileManager fileManager, final ClassLoader parentClassLoader) {
        super(fileManager);

        this.parentClassLoader = parentClassLoader;
    }

    private Collection<ClassScriptMemoryJavaObject> memoryClasses() {
        return mapNameToClasses.values();
    }

    public FileScriptMemoryJavaObject createSourceFileObject(final File origin, final String name, final String code) {
        return new FileScriptMemoryJavaObject(origin, name, JavaFileObject.Kind.SOURCE, code);
    }

    public ScriptClassLoader getClassLoader(final ScriptLoader loader, final File file, final String fullClassName, final FileScriptMemoryJavaObject source) throws ScriptException, MalformedURLException {
        return new ScriptClassLoader(loader, mapNameToClasses, parentClassLoader, file, fullClassName, source);
    }

    @Override
    public Iterable<JavaFileObject> list(
            final JavaFileManager.Location location,
            final String packageName,
            final Set<JavaFileObject.Kind> kinds,
            final boolean recurse) throws IOException {
        final Iterable<JavaFileObject> list = super.list(location, packageName, kinds, recurse);
        if (location == CLASS_OUTPUT) {
            final Collection<? extends JavaFileObject> generatedClasses = memoryClasses();
            return () -> new CompositeIterator<>(
                    list.iterator(),
                    generatedClasses.iterator());
        }

        return list;
    }

    @Override
    public String inferBinaryName(final JavaFileManager.Location location, final JavaFileObject file) {
        if (file instanceof ClassScriptMemoryJavaObject) {
            return file.getName();
        } else {
            return super.inferBinaryName(location, file);
        }
    }

    @Override
    public JavaFileObject getJavaFileForOutput(final JavaFileManager.Location location, final String className, final JavaFileObject.Kind kind, final FileObject sibling) throws IOException {
        if (kind == JavaFileObject.Kind.CLASS) {
            final ClassScriptMemoryJavaObject file = new ClassScriptMemoryJavaObject(className);
            mapNameToClasses.put(className, file);
            Debug.console("Mapping class " + className);
            return file;
        }

        return super.getJavaFileForOutput(location, className, kind, sibling);
    }
}
