package xyz.destiall.caramel.api.scripts;

import java.io.File;

public interface ScriptManager {
    InternalScript reloadScript(File file);
    void reloadAll();
    void destroy();
    void loadScripts(File folder);
    InternalScript getScript(String name);
}
