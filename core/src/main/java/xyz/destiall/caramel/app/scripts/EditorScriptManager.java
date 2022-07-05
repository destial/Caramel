package xyz.destiall.caramel.app.scripts;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.debug.DebugImpl;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.objects.GameObjectImpl;
import xyz.destiall.caramel.api.scripts.InternalScript;
import xyz.destiall.caramel.api.scripts.Script;
import xyz.destiall.caramel.api.scripts.ScriptManager;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.events.FileEvent;
import xyz.destiall.caramel.app.scripts.loader.ScriptLoader;
import xyz.destiall.caramel.app.utils.Payload;
import xyz.destiall.java.events.EventHandler;
import xyz.destiall.java.events.Listener;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class EditorScriptManager implements ScriptManager, Listener {
    private final File scriptsRootFolder;
    private final Map<String, InternalScript> compiledScripts;
    private final Set<String> awaitingCompilation;
    private FileWatcher watcher;
    private final ScriptLoader loader;

    public EditorScriptManager() {
        scriptsRootFolder = new File("assets/scripts/");
        scriptsRootFolder.mkdir();
        compiledScripts = new ConcurrentHashMap<>();
        awaitingCompilation = ConcurrentHashMap.newKeySet();

        if (ApplicationImpl.getApp().EDITOR_MODE) {
            watcher = new FileWatcher(scriptsRootFolder);
        }

        loader = new ScriptLoader(this);
    }

    public void reloadAll() {
        if (!scriptsRootFolder.exists()) scriptsRootFolder.mkdir();
        loadScripts(scriptsRootFolder);

        if (watcher != null) watcher.watch();
    }

    public void destroy() {
        if (watcher != null) watcher.destroy();
    }

    public void loadScripts(File folder) {
        compiledScripts.clear();
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) return;
        List<File> sort = Arrays.asList(files);
        loadSort(sort);
    }

    private void loadSort(List<File> files) {
        int passes = 0;
        List<File> notLoaded = new ArrayList<>(files);
        for (File file : files) {
            if (file.isDirectory()) {
                loadScripts(file);
                continue;
            }
            try {
                InternalScript script = reloadScript(file);
                if (script != null) {
                    passes++;
                    notLoaded.remove(file);
                }
            } catch (Exception ignored) {}
        }
        if (passes != 0 && notLoaded.size() != 0) {
            loadSort(notLoaded);
        }
    }

    @Override
    public InternalScript getScript(String name) {
        return compiledScripts.get(name);
    }

    @Override
    public InternalScript getInternalScript(Class<?> clazz) {
        return compiledScripts.values().stream().filter(c -> c.getCompiledClass().isAssignableFrom(clazz)).findFirst().orElse(null);
    }

    @Override
    public InternalScript reloadScript(File file) {
        if (file.getName().endsWith(".java")) {
            String scriptName = file.getName().substring(0, file.getName().length() - ".java".length());
            if (compiledScripts.containsKey(scriptName)) return compiledScripts.get(scriptName);
            try {
                InternalScript compiledScript = loader.compile(file);
                if (compiledScript.getCompiledClass().isAssignableFrom(Script.class)) {
                    DebugImpl.logError("Script " + scriptName + " does not inherit Script class!");
                    return null;
                }
                compiledScripts.put(scriptName, compiledScript);
                Payload.COMPONENTS.add(compiledScript.getCompiledClass());
                return compiledScript;
            } catch (Exception e) {
                DebugImpl.logError(e.getLocalizedMessage());
            }
        }
        return null;
    }

    @Override
    public InternalScript reloadScript(File file, String contents) {
        if (file.getName().endsWith(".java")) {
            String scriptName = file.getName().substring(0, file.getName().length() - ".java".length());
            if (compiledScripts.containsKey(scriptName)) return compiledScripts.get(scriptName);
            try {
                InternalScript compiledScript = loader.compile(file, contents);
                if (compiledScript.getCompiledClass().isAssignableFrom(Component.class)) {
                    DebugImpl.logError("Script " + scriptName + " does not inherit Component class!");
                    return null;
                }
                compiledScripts.put(scriptName, compiledScript);
                Payload.COMPONENTS.add(compiledScript.getCompiledClass());
                return compiledScript;
            } catch (Exception e) {
                DebugImpl.logError(e.getLocalizedMessage());
            }
        }
        return null;
    }

    public boolean uploadScript(GameObjectImpl object, String script) {
        InternalScript scriptComponent = compiledScripts.get(script);
        if (scriptComponent == null) return false;
        try {
            Component component = scriptComponent.getAsComponent(object);
            return object.addComponent(component);
        } catch (Exception e) {
            DebugImpl.logError(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeScript(GameObjectImpl object, String script) {
        InternalScript scriptComponent = compiledScripts.get(script);
        if (scriptComponent == null) return false;
        return object.removeComponent(scriptComponent.getCompiledClass());
    }

    public Collection<String> getScripts() {
        return compiledScripts.keySet();
    }

    @EventHandler
    private void onFileModify(FileEvent event) {
        if (event.getFile().getName().endsWith(".java")) {
            File file = new File(scriptsRootFolder, event.getFile().getName());
            String scriptName = file.getName().substring(0, file.getName().length() - ".java".length());
            InternalScript script = compiledScripts.get(scriptName);

            if (event.getType() == FileEvent.Type.MODIFY || event.getType() == FileEvent.Type.CREATE) {
                if (!awaitingCompilation.add(file.getName())) {
                    return;
                }
                if (script != null && event.getType() == FileEvent.Type.MODIFY) {
                    System.out.println("Modified file: " + event.getFile().getName());
                    compiledScripts.remove(scriptName);
                    Payload.COMPONENTS.remove(script.getCompiledClass());

                    System.gc();
                    try {
                        InternalScript newScript = reloadScript(file);

                        for (GameObject go : ApplicationImpl.getApp().getCurrentScene().getGameObjects()) {
                            Component instance;
                            if ((instance = go.getComponent(script.getCompiledClass())) != null) {
                                Field[] fields = script.getCompiledClass().getFields();
                                if (go.removeComponent(script.getCompiledClass())) {
                                    Component component = newScript.getAsComponent(go);
                                    for (Field field : fields) {
                                        if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
                                        Field newField = component.getClass().getField(field.getName());
                                        if (newField.getType() == field.getType()) {
                                            newField.setAccessible(true);
                                            try {
                                                newField.set(component, field.get(instance));
                                            } catch (IllegalAccessException e) {
                                                DebugImpl.logError(e.getMessage());
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    go.addComponent(component);
                                }
                            }
                        }
                        awaitingCompilation.remove(file.getName());
                    } catch (Exception e) {
                        DebugImpl.logError(e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    reloadScript(file);
                    System.gc();
                }
            } else {
                if (script != null) {
                    ApplicationImpl.getApp().getCurrentScene().forEachGameObject(go -> go.removeComponent(script.getCompiledClass()));
                    compiledScripts.remove(scriptName);
                    Payload.COMPONENTS.remove(script.getCompiledClass());
                }
            }
        }
    }
}
