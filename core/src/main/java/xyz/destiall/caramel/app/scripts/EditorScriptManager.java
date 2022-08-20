package xyz.destiall.caramel.app.scripts;

import caramel.api.Component;
import caramel.api.debug.Debug;
import caramel.api.debug.DebugImpl;
import caramel.api.events.FileEvent;
import caramel.api.events.ScenePlayEvent;
import caramel.api.events.SceneStopEvent;
import caramel.api.events.WindowFocusEvent;
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
import xyz.destiall.java.timer.Task;

import javax.script.ScriptException;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EditorScriptManager implements ScriptManager, Listener {
    private final File scriptsRootFolder;
    private final Map<String, File> awaitingCompilation;
    private final Map<String, FileEvent> awaitingEvents;
    private final ScriptLoader loader;

    private FileWatcher watcher;
    private Task compileTask;
    private boolean isPlaying = false;

    public EditorScriptManager() {
        scriptsRootFolder = new File("assets" + File.separator + "scripts" + File.separator);
        if (scriptsRootFolder.mkdir()) {
            FileIO.saveResource("CharacterController2D.java", "assets" + File.separator + "scripts" + File.separator + "CharacterController2D.java");
        }
        awaitingCompilation = new ConcurrentHashMap<>();
        awaitingEvents = new HashMap<>();
        if (ApplicationImpl.getApp().EDITOR_MODE) {
            watcher = new FileWatcher(scriptsRootFolder);
        }

        loader = new ScriptLoader(this);
    }

    @Override
    public void reloadAll() {
        if (!scriptsRootFolder.exists()) {
            if (scriptsRootFolder.mkdir()) {
                FileIO.saveResource("CharacterController2D.java", "assets" + File.separator + "scripts" + File.separator + "CharacterController2D.java");
            }
        }
        loadScripts(scriptsRootFolder);

        if (watcher != null) watcher.watch();
    }

    public void build(File output) {
        try {
            File root = new File("temp" + File.separator);
            if (root.exists()) {
                FileIO.delete(root);
            }
            root.mkdir();
            loader.build(root, output);
        } catch (Exception e) {
            Debug.logError(e.getMessage());
            e.printStackTrace();
        }
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
        try {
            loadIntoLoader(sort);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private void loadIntoLoader(Collection<File> files) throws ScriptException {
        loader.compileAll(new ArrayList<>(files));
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
        String contents = FileIO.readData(file);
        if (contents == null) return null;
        return reloadScript(file, contents);
    }

    @Override
    public InternalScript reloadScript(File file, String contents) {
        if (file.getName().endsWith(".java")) {
            String scriptName = file.getName().substring(0, file.getName().length() - 5 /* ".java".length() */);
            InternalScript previous = getScript(scriptName);
            if (previous != null) {
                Payload.COMPONENTS.remove(previous.getCompiledClass());
            }
            try {
                InternalScript compiledScript = loader.compile(file, contents);
                System.gc();
                System.gc();
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
                if ((instance = go.getComponent(oldScript.getCompiledClass().getSimpleName())) != null) {
                    Field[] fields = oldScript.getCompiledClass().getFields();
                    if (go.removeComponent(oldScript.getCompiledClass().getSimpleName())) {
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
            System.gc();
            System.gc();
        } catch (Exception e) {
            DebugImpl.logError("Error reloading script component " + newScript.getFile().getName());
            e.printStackTrace();
        }
    }

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
    private void onFocus(WindowFocusEvent e) {
        if (e.isFocused() && !awaitingCompilation.isEmpty()) {
            startCompileTask();
        }
    }

    public void startCompileTask() {
        if (compileTask != null) return;
        compileTask = ApplicationImpl.getApp().getScheduler().runTaskLater(() -> {
            for (File file : awaitingCompilation.values()) {
                try {
                    reloadScript(file);
                    System.gc();
                } catch (Exception e) {
                    DebugImpl.logError(e.getMessage());
                }
            }
            awaitingCompilation.clear();
            compileTask = null;
        }, 10);
    }

    public void setCompileTask(Task task) {
        this.compileTask = task;
    }

    public boolean isRecompiling() {
        return compileTask != null;
    }

    @EventHandler
    private void onFileModify(FileEvent event) {
        if (event.getFile().getName().endsWith(".java")) {
            File file = new File(scriptsRootFolder, event.getFile().getName());
            String scriptName = file.getName().substring(0, file.getName().length() - 5 /* ".java".length() */);
            InternalScript script = getScript(scriptName);
            if (isPlaying) {
                awaitingEvents.put(file.getPath(), event);
                return;
            }
            if (event.getType() == FileEvent.Type.MODIFY || event.getType() == FileEvent.Type.CREATE) {
                if (awaitingCompilation.containsKey(file.getName())) return;
                awaitingCompilation.put(file.getName(), file);
            } else {
                if (script != null) {
                    for (Scene scene : ApplicationImpl.getApp().getScenes()) {
                        scene.forEachGameObject(go -> go.removeComponent(script.getCompiledClass()));
                    }
                    loader.removeScript(scriptName);
                    Payload.COMPONENTS.remove(script.getCompiledClass());
                }
            }
        }
    }
}
