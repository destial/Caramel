package xyz.destiall.caramel.app.scripts.loader;

import ch.obermuhlner.scriptengine.java.util.CompositeIterator;

import javax.script.ScriptException;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static javax.tools.StandardLocation.CLASS_OUTPUT;

public final class ScriptMemoryManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, ClassScriptMemoryJavaObject> mapNameToClasses = new ConcurrentHashMap<>();
    private final ClassLoader parentClassLoader;

    public ScriptMemoryManager(JavaFileManager fileManager, ClassLoader parentClassLoader) {
        super(fileManager);

        this.parentClassLoader = parentClassLoader;
    }

    private Collection<ClassScriptMemoryJavaObject> memoryClasses() {
        return mapNameToClasses.values();
    }

    public ScriptMemoryJavaObject createSourceFileObject(Object origin, String name, String code) {
        return new ScriptMemoryJavaObject(origin, name, JavaFileObject.Kind.SOURCE, code);
    }

    public ScriptClassLoader getClassLoader(ScriptLoader loader, File file, String fullClassName, String simpleClassName) throws ScriptException, MalformedURLException {
        Map<String, byte[]> mapNameToBytes = new ConcurrentHashMap<>();
        for (ClassScriptMemoryJavaObject outputMemoryJavaFileObject : memoryClasses()) {
            mapNameToBytes.put(
                    outputMemoryJavaFileObject.getName(),
                    outputMemoryJavaFileObject.getBytes());
        }
        return new ScriptClassLoader(loader, mapNameToBytes, parentClassLoader, file, fullClassName, simpleClassName);
    }

    @Override
    public Iterable<JavaFileObject> list(
            JavaFileManager.Location location,
            String packageName,
            Set<JavaFileObject.Kind> kinds,
            boolean recurse) throws IOException {
        Iterable<JavaFileObject> list = super.list(location, packageName, kinds, recurse);

        if (location == CLASS_OUTPUT) {
            Collection<? extends JavaFileObject> generatedClasses = memoryClasses();
            return () -> new CompositeIterator<>(
                    list.iterator(),
                    generatedClasses.iterator());
        }

        return list;
    }

    @Override
    public String inferBinaryName(JavaFileManager.Location location, JavaFileObject file) {
        if (file instanceof ClassScriptMemoryJavaObject) {
            return file.getName();
        } else {
            return super.inferBinaryName(location, file);
        }
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
            JavaFileManager.Location location,
            String className,
            JavaFileObject.Kind kind,
            FileObject sibling)
            throws IOException {
        if (kind == JavaFileObject.Kind.CLASS) {
            ClassScriptMemoryJavaObject file = new ClassScriptMemoryJavaObject(className);
            mapNameToClasses.put(className, file);
            return file;
        }

        return super.getJavaFileForOutput(location, className, kind, sibling);
    }

    static abstract class AbstractScriptJavaObject extends SimpleJavaFileObject {
        public AbstractScriptJavaObject(String name, JavaFileObject.Kind kind) {
            super(URI.create("memory:///" +
                    name.replace('.', '/') +
                    kind.extension), kind);
        }
    }

    static class ScriptMemoryJavaObject extends AbstractScriptJavaObject {
        private final Object origin;
        private final String code;

        ScriptMemoryJavaObject(Object origin, String className, JavaFileObject.Kind kind, String code) {
            super(className, kind);

            this.origin = origin;
            this.code = code;
        }

        public Object getOrigin() {
            return origin;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    static class ClassScriptMemoryJavaObject extends AbstractScriptJavaObject {

        private ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        private transient byte[] bytes = null;

        private final String className;

        public ClassScriptMemoryJavaObject(String className) {
            super(className, JavaFileObject.Kind.CLASS);

            this.className = className;
        }

        public byte[] getBytes() {
            if (bytes == null) {
                bytes = byteOutputStream.toByteArray();
                byteOutputStream = null;
            }
            return bytes;
        }

        @Override
        public String getName() {
            return className;
        }

        @Override
        public OutputStream openOutputStream() {
            return byteOutputStream;
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(getBytes());
        }
    }

}
