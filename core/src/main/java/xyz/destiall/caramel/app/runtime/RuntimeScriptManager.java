package xyz.destiall.caramel.app.runtime;

import caramel.api.scripts.InternalScript;
import caramel.api.scripts.ScriptManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

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
    public void loadScripts(File folder) {
        try {
            URL url = folder.toURI().toURL();
            URL[] urls = new URL[]{url};
            scriptLoader = new URLClassLoader(urls, getClass().getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public InternalScript getScript(String name) {
        Map.Entry<Class<?>, InternalScript> entry = scripts.entrySet().stream().filter(en -> en.getKey().getSimpleName().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (entry == null) {
            try {
                Class<?> cls = scriptLoader.loadClass(name);
                InternalScript internalScript = new InternalScript(cls, null, null);
                scripts.put(cls, internalScript);
                return internalScript;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return entry.getValue();
    }

    @Override
    public InternalScript getInternalScript(Class<?> clazz) {
        return scripts.get(clazz);
    }

    @Override
    public InternalScript reloadScript(File file) {
        return null;
    }

    @Override
    public InternalScript reloadScript(File file, String contents) {
        return null;
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
