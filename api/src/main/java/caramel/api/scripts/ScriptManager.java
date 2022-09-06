package caramel.api.scripts;

import java.io.File;

public interface ScriptManager {
    InternalScript reloadScript(final File file);
    InternalScript reloadScript(final File file, final String contents);
    void reloadAll();
    void destroy();
    void loadScripts(final File folder);
    InternalScript getInternalScript(final Class<?> clazz);
    InternalScript getScript(final String name);
}
