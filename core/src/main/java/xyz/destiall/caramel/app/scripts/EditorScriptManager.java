package xyz.destiall.caramel.app.scripts;

import caramel.api.Component;
import caramel.api.debug.DebugImpl;
import caramel.api.events.FileEvent;
import caramel.api.events.ScenePlayEvent;
import caramel.api.events.SceneStopEvent;
import caramel.api.objects.GameObject;
import caramel.api.objects.Scene;
import caramel.api.objects.SceneImpl;
import caramel.api.scripts.InternalScript;
import caramel.api.scripts.Script;
import caramel.api.scripts.ScriptManager;
import caramel.api.utils.FileIO;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.scripts.loader.ScriptLoader;
import xyz.destiall.caramel.app.utils.Payload;
import xyz.destiall.java.events.EventHandler;
import xyz.destiall.java.events.Listener;

import javax.script.ScriptException;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class EditorScriptManager implements ScriptManager, Listener {
    private final File scriptsRootFolder;
    private final Set<String> awaitingCompilation;
    private FileWatcher watcher;
    private final ScriptLoader loader;

    public EditorScriptManager() {
        scriptsRootFolder = new File("assets/scripts/");
        if (scriptsRootFolder.mkdir()) {
            FileIO.saveResource("CharacterController2D.java", "assets/scripts/CharacterController2D.java");
        }

        awaitingCompilation = ConcurrentHashMap.newKeySet();

        if (ApplicationImpl.getApp().EDITOR_MODE) {
            watcher = new FileWatcher(scriptsRootFolder);
        }

        loader = new ScriptLoader(this);
    }

    @Override
    public void reloadAll() {
        if (!scriptsRootFolder.exists()) {
            if (scriptsRootFolder.mkdir()) {
                FileIO.saveResource("CharacterController2D.java", "assets/scripts/CharacterController2D.java");
            }
        }
        loadScripts(scriptsRootFolder);

        if (watcher != null) watcher.watch();
    }

    @Override
    public void destroy() {
        if (watcher != null) watcher.destroy();
    }

    @Override
    public void loadScripts(File folder) {
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) return;
        List<File> sort = Arrays.asList(files);
        //loadSort(sort);
        try {
            loadIntoLoader(sort);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private void loadIntoLoader(List<File> files) throws ScriptException {
        loader.compileAll(files);
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
        return loader.get(name);
    }

    @Override
    public InternalScript getInternalScript(Class<?> clazz) {
        return loader.getByClass(clazz);
    }

    @Override
    public InternalScript reloadScript(File file) {
        if (file.getName().endsWith(".java")) {
            String scriptName = file.getName().substring(0, file.getName().length() - ".java".length());
            InternalScript previous = loader.removeScript(scriptName);
            if (previous != null) {
                Payload.COMPONENTS.remove(previous.getCompiledClass());
            }
            try {
                InternalScript compiledScript = loader.compile(file);
                if (compiledScript.getCompiledClass().isAssignableFrom(Script.class)) {
                    DebugImpl.logError("Script " + scriptName + " does not inherit Script class!");
                    return null;
                }
                Payload.COMPONENTS.add(compiledScript.getCompiledClass());
                return compiledScript;
            } catch (Exception e) {
                DebugImpl.logError(e.getMessage());
                if (previous != null) {
                    Payload.COMPONENTS.add(previous.getCompiledClass());
                }
            }
        }
        return null;
    }

    @Override
    public InternalScript reloadScript(File file, String contents) {
        if (file.getName().endsWith(".java")) {
            String scriptName = file.getName().substring(0, file.getName().length() - ".java".length());
            InternalScript previous = getScript(scriptName);
            if (previous != null) {
                Payload.COMPONENTS.remove(previous.getCompiledClass());
            }
            try {
                InternalScript compiledScript = loader.compile(file, contents);
                if (compiledScript.getCompiledClass().isAssignableFrom(Script.class)) {
                    DebugImpl.logError("Script " + scriptName + " does not inherit Script class!");
                    return null;
                }
                Payload.COMPONENTS.add(compiledScript.getCompiledClass());
                return compiledScript;
            } catch (Exception e) {
                DebugImpl.logError(e.getMessage());
                if (previous != null) {
                    Payload.COMPONENTS.add(previous.getCompiledClass());
                }
            }
        }
        return null;
    }

    public void loadComponents(InternalScript oldScript, InternalScript newScript) {
        try {
            SceneImpl scene = ApplicationImpl.getApp().getCurrentScene();
            if (scene == null) return;
            for (GameObject go : scene.getGameObjects()) {
                Component instance;
                if ((instance = go.getComponent(oldScript.getCompiledClass())) != null) {
                    Field[] fields = oldScript.getCompiledClass().getFields();
                    if (go.removeComponent(oldScript.getCompiledClass())) {
                        Component component = newScript.getAsComponent(go);
                        for (Field field : fields) {
                            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                                continue;
                            }
                            Field newField = component.getClass().getField(field.getName());
                            if (newField.getType() == field.getType()) {
                                newField.setAccessible(true);
                                try {
                                    newField.set(component, field.get(instance));
                                } catch (Exception e) {
                                    DebugImpl.logError("Error setting field on script component " + newScript.getFile().getName());
                                    e.printStackTrace();
                                }
                            }
                        }
                        go.addComponent(component);
                    }
                }
            }
        } catch (Exception e) {
            DebugImpl.logError("Error reloading script component " + newScript.getFile().getName());
            e.printStackTrace();
        }
    }

    private final Map<String, FileEvent> awaitingEvents = new HashMap<>();
    private boolean isPlaying = false;

    @EventHandler
    private void onScenePlay(ScenePlayEvent e) {
        isPlaying = true;
    }

    @EventHandler
    private void onSceneStop(SceneStopEvent e) {
        isPlaying = false;
        for (FileEvent event : awaitingEvents.values()) {
            onFileModify(event);
        }
        awaitingEvents.clear();
    }

    @EventHandler
    private void onFileModify(FileEvent event) {
        if (event.getFile().getName().endsWith(".java")) {
            File file = new File(scriptsRootFolder, event.getFile().getName());
            String scriptName = file.getName().substring(0, file.getName().length() - ".java".length());
            InternalScript script = getScript(scriptName);
            if (isPlaying) {
                awaitingEvents.put(file.getPath(), event);
                return;
            }
            if (event.getType() == FileEvent.Type.MODIFY || event.getType() == FileEvent.Type.CREATE) {
                if (!awaitingCompilation.add(file.getName())) {
                    return;
                }
                if (script != null && event.getType() == FileEvent.Type.MODIFY) {
                    try {
                        InternalScript newScript = reloadScript(file);
                        if (newScript != null) {
                            loadComponents(script, newScript);
                        }
                        System.gc();
                    } catch (Exception e) {
                        DebugImpl.logError(e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    reloadScript(file);
                    System.gc();
                }
                awaitingCompilation.remove(file.getName());
            } else {
                if (script != null) {
                    Scene scene = ApplicationImpl.getApp().getCurrentScene();
                    if (scene != null) {
                        scene.forEachGameObject(go -> go.removeComponent(script.getCompiledClass()));
                    }
                    loader.removeScript(scriptName);
                    Payload.COMPONENTS.remove(script.getCompiledClass());
                }
            }
        }
    }
}
