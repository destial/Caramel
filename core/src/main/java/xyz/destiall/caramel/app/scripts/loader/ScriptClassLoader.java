package xyz.destiall.caramel.app.scripts.loader;

import xyz.destiall.caramel.api.scripts.InternalScript;

import javax.script.ScriptException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Map;

public final class ScriptClassLoader extends URLClassLoader {
    private final ScriptLoader loader;
    public static final String MEMORY_CLASS_URL = "http://ch.obermuhlner/ch.obermuhlner.scriptengine.java/memory-class";

    private final ProtectionDomain protectionDomain;
    private final Map<String, byte[]> mapClassBytes;
    private final File file;
    public final InternalScript script;
    private final FileScriptMemoryJavaObject source;

    ScriptClassLoader(ScriptLoader loader, Map<String, byte[]> mapNameToBytes, ClassLoader parent, File file, String fullClassName, FileScriptMemoryJavaObject source) throws ScriptException, MalformedURLException {
        super(new URL[] {file.toURI().toURL()}, parent);
        this.mapClassBytes = mapNameToBytes;
        try {
            URL url = new URL(MEMORY_CLASS_URL);
            CodeSource codeSource = new CodeSource(url, (Certificate[]) null);
            protectionDomain = new ProtectionDomain(codeSource, null, this, new Principal[0]);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.loader = loader;
        this.file = file;
        this.source = source;
        try {
            Class<?> clazz = loadClass(fullClassName);
            this.script = new InternalScript(clazz, file, this.source.getCharContent(false));
        } catch (ClassNotFoundException e) {
            throw new ScriptException(e);
        }
    }

    public FileScriptMemoryJavaObject getSource() {
        return source;
    }

    public File getFile() {
        return file;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = loader.getClassByName(name);
        if (result == null) {
            result = super.findClass(name);
            if (result != null) {
                loader.setClass(name, result);
            }
        }
        return result;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        byte[] bytes = mapClassBytes.get(name);
        if (bytes == null) {
            return super.loadClass(name);
        }
        return defineClass(name, bytes, 0, bytes.length, protectionDomain);
    }
}
