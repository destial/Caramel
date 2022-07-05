package xyz.destiall.caramel.api.scripts;

import java.io.File;

public interface ScriptManager {
    InternalScript reloadScript(File file);
    InternalScript reloadScript(File file, String contents);
    void reloadAll();
    void destroy();
    void loadScripts(File folder);
    InternalScript getInternalScript(Class<?> clazz);
    InternalScript getScript(String name);
}
