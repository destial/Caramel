package xyz.destiall.caramel.app.runtime;

import caramel.api.scripts.InternalScript;
import caramel.api.scripts.ScriptManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public final class RuntimeScriptManager implements ScriptManager {
    private final File scriptsRootFolder;
    private final Map<Class<?>, InternalScript> scripts;
    private URLClassLoader scriptLoader;

    public RuntimeScriptManager() {
        scriptsRootFolder = new File("scripts" + File.separator);
        scripts = new HashMap<>();
    }

    @Override
    public void reloadAll() {
        loadScripts(scriptsRootFolder);
    }

    @Override
    public void destroy() {
        try {
            scriptLoader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadScripts(final File folder) {
        try {
            final URL url = folder.toURI().toURL();
            final URL[] urls = new URL[]{url};
            scriptLoader = new URLClassLoader(urls, getClass().getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public InternalScript getScript(String name) {
        final Map.Entry<Class<?>, InternalScript> entry = scripts.entrySet().stream().filter(en -> en.getKey().getSimpleName().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (entry == null) {
            try {
                final Class<?> cls = scriptLoader.loadClass(name);
                final InternalScript internalScript = new InternalScript(cls, null, null);
                scripts.put(cls, internalScript);
                return internalScript;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return entry.getValue();
    }

    @Override
    public InternalScript getInternalScript(final Class<?> clazz) {
        return scripts.get(clazz);
    }

    @Override
    public InternalScript reloadScript(final File file) {
        return null;
    }

    @Override
    public InternalScript reloadScript(final File file, final String contents) {
        return null;
    }
}
