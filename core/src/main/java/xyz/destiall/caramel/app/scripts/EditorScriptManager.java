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

        loader = new ScriptLoader(this);
        if (!loader.canLoad()) return;
        watcher = new FileWatcher(scriptsRootFolder);
    }

    public boolean canLoad() {
        return loader.canLoad();
    }

    @Override
    public void reloadAll() {
        if (!loader.canLoad()) return;
        if (!scriptsRootFolder.exists()) {
            if (scriptsRootFolder.mkdir()) {
                FileIO.saveResource("CharacterController2D.java", "assets" + File.separator + "scripts" + File.separator + "CharacterController2D.java");
            }
        }
        loadScripts(scriptsRootFolder);

        if (watcher != null) watcher.watch();
    }

    public void build(File output) {
        if (!loader.canLoad()) return;

        try {
            final File root = new File("temp" + File.separator);
            Debug.log("Building to " + output);
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
        if (!loader.canLoad()) return;
        final File[] files = folder.listFiles();
        if (files == null || files.length == 0) return;
        final List<File> sort = Arrays.asList(files);
        try {
            loadIntoLoader(sort);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private void loadIntoLoader(Collection<File> files) throws ScriptException {
        if (!loader.canLoad()) return;
        loader.compileAll(new ArrayList<>(files));
    }

    @Override
    public InternalScript getScript(String name) {
        if (!loader.canLoad()) return null;
        return loader.get(name);
    }

    @Override
    public InternalScript getInternalScript(Class<?> clazz) {
        if (!loader.canLoad()) return null;
        return loader.getByClass(clazz);
    }

    @Override
    public InternalScript reloadScript(File file) {
        final String contents = FileIO.readData(file);
        if (contents == null) return null;
        return reloadScript(file, contents);
    }

    @Override
    public InternalScript reloadScript(File file, String contents) {
        if (!loader.canLoad()) return null;
        if (file.getName().toLowerCase().endsWith(".java")) {
            final String scriptName = file.getName().substring(0, file.getName().length() - 5 /* ".java".length() */);
            final InternalScript previous = getScript(scriptName);
            if (previous != null) {
                Payload.COMPONENTS.remove(previous.getCompiledClass());
            }
            try {
                final InternalScript compiledScript = loader.compile(file, contents);
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
            final SceneImpl scene = ApplicationImpl.getApp().getCurrentScene();
            if (scene == null) return;
            for (final GameObject go : scene.getGameObjects()) {
                Component instance;
                if ((instance = go.getComponent(oldScript.getCompiledClass().getSimpleName())) != null) {
                    final Field[] fields = oldScript.getCompiledClass().getFields();
                    if (go.removeComponent(oldScript.getCompiledClass().getSimpleName())) {
                        final Component component = newScript.getAsComponent(go);
                        for (final Field field : fields) {
                            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                                continue;
                            }
                            final Field newField = component.getClass().getField(field.getName());
                            if (newField.getType() == field.getType()) {
                                newField.setAccessible(true);
                                try {
                                    newField.set(component, field.get(instance));
                                } catch (final Exception e) {
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
        } catch (final Exception e) {
            DebugImpl.logError("Error reloading script component " + newScript.getFile().getName());
            e.printStackTrace();
        }
    }

    @EventHandler
    private void onScenePlay(final ScenePlayEvent e) {
        isPlaying = true;
    }

    @EventHandler
    private void onSceneStop(final SceneStopEvent e) {
        isPlaying = false;
        for (final FileEvent event : awaitingEvents.values()) {
            onFileModify(event);
        }
        awaitingEvents.clear();
    }

    @EventHandler
    private void onFocus(final WindowFocusEvent e) {
        if (e.isFocused() && !awaitingCompilation.isEmpty()) {
            startCompileTask();
        }
    }

    public void startCompileTask() {
        if (!loader.canLoad()) return;

        if (compileTask != null) return;
        compileTask = ApplicationImpl.getApp().getScheduler().runTaskLater(() -> {
            for (final File file : awaitingCompilation.values()) {
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

    public void setCompileTask(final Task task) {
        this.compileTask = task;
    }

    public boolean isRecompiling() {
        return compileTask != null;
    }

    @EventHandler
    private void onFileModify(final FileEvent event) {
        if (!loader.canLoad()) return;
        if (event.getFile().getName().toLowerCase().endsWith(".java")) {
            final File file = new File(scriptsRootFolder, event.getFile().getName());
            final String scriptName = file.getName().substring(0, file.getName().length() - 5 /* ".java".length() */);
            final InternalScript script = getScript(scriptName);
            if (isPlaying) {
                awaitingEvents.put(file.getPath(), event);
                return;
            }
            if (event.getType() == FileEvent.Type.MODIFY || event.getType() == FileEvent.Type.CREATE) {
                if (awaitingCompilation.containsKey(file.getName())) return;
                awaitingCompilation.put(file.getName(), file);
            } else {
                if (script != null) {
                    for (final Scene scene : ApplicationImpl.getApp().getSceneLoader().getScenes()) {
                        scene.forEachGameObject(go -> go.removeComponent(script.getCompiledClass()));
                    }
                    loader.removeScript(scriptName);
                    Payload.COMPONENTS.remove(script.getCompiledClass());
                }
            }
        }
    }
}
