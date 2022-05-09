package xyz.destiall.caramel.app.scripts;

import ch.obermuhlner.scriptengine.java.JavaCompiledScript;
import ch.obermuhlner.scriptengine.java.JavaScriptEngine;
import ch.obermuhlner.scriptengine.java.JavaScriptEngineFactory;
import ch.obermuhlner.scriptengine.java.MemoryFileManager;
import ch.obermuhlner.scriptengine.java.constructor.NullConstructorStrategy;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.api.Debug;
import xyz.destiall.caramel.app.events.FileEvent;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.editor.ui.InspectorPanel;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.java.events.EventHandler;
import xyz.destiall.java.events.Listener;
import xyz.destiall.java.reflection.Reflect;

import javax.script.ScriptException;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptManager implements Listener {
    private final FileWatcher watcher;
    private final File scriptsRootFolder;
    private final JavaScriptEngine engine;
    private final Map<String, JavaCompiledScript> compiledScripts;
    private final Set<String> awaitingCompilation;

    public ScriptManager() {
        scriptsRootFolder = new File("assets/scripts/");
        scriptsRootFolder.mkdir();
        engine = (JavaScriptEngine) new JavaScriptEngineFactory().getScriptEngine();
        engine.setConstructorStrategy((clazz) -> new NullConstructorStrategy());
        compiledScripts = new ConcurrentHashMap<>();
        awaitingCompilation = new HashSet<>();

        watcher = new FileWatcher(scriptsRootFolder);
    }

    public void reloadAll() {
        if (!scriptsRootFolder.exists()) scriptsRootFolder.mkdir();
        loadScripts(scriptsRootFolder);

        watcher.watch();
    }

    public void destroy() {
        watcher.destroy();
    }

    public void loadScripts(File folder) {
        compiledScripts.clear();
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) return;
        for (File file : files) {
            if (file.isDirectory()) {
                loadScripts(file);
                continue;
            }
            reloadScript(file);
        }
    }

    public JavaCompiledScript reloadScript(File file) {
        if (file.getName().endsWith(".java")) {
            String scriptName = file.getName().substring(0, file.getName().length() - ".java".length());
            if (compiledScripts.containsKey(scriptName)) return compiledScripts.get(scriptName);
            String contents = readScript(file);
            try {
                JavaCompiledScript compiledScript = engine.compile(contents);
                if (compiledScript.getCompiledClass().isAssignableFrom(Component.class)) {
                    Debug.logError("Script " + scriptName + " does not inherit Component class!");
                    return null;
                }
                compiledScripts.put(scriptName, compiledScript);
                InspectorPanel.COMPONENTS.add(compiledScript.getCompiledClass());
                return compiledScript;
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String readScript(File file) {
        try {
            Scanner scanner = new Scanner(file);
            StringBuilder contents = new StringBuilder();
            while (scanner.hasNextLine()) {
                contents.append(scanner.nextLine()).append("\n");
            }
            return contents.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean uploadScript(GameObject object, String script) {
        JavaCompiledScript scriptComponent = compiledScripts.get(script);
        if (scriptComponent == null) return false;
        Component component = (Component) Reflect.newInstance(scriptComponent.getCompiledClass(), object);
        return object.addComponent(component);
    }

    public boolean removeScript(GameObject object, String script) {
        JavaCompiledScript scriptComponent = compiledScripts.get(script);
        if (scriptComponent == null) return false;
        return object.removeComponent((Class<? extends Component>) scriptComponent.getCompiledClass());
    }

    public Collection<String> getScripts() {
        return compiledScripts.keySet();
    }

    @EventHandler
    private void onFileModify(FileEvent event) {
        if (event.getFile().getName().endsWith(".java")) {
            File file = new File(scriptsRootFolder, event.getFile().getName());
            String scriptName = file.getName().substring(0, file.getName().length() - ".java".length());
            JavaCompiledScript script = compiledScripts.get(scriptName);
            if (event.getType() == FileEvent.Type.MODIFY || event.getType() == FileEvent.Type.CREATE) {
                System.out.println("Modified file: " + event.getFile().getName());
                if (!awaitingCompilation.add(file.getName())) {
                    return;
                }
                if (script != null && event.getType() == FileEvent.Type.MODIFY) {
                    final List<GameObject> reloadedGameobjects = new ArrayList<>();
                    Application.getApp().getCurrentScene().forEachGameObject((go) -> {
                        if (go.removeComponent((Class<? extends Component>) script.getCompiledClass())) {
                            reloadedGameobjects.add(go);
                        }
                    });
                    compiledScripts.remove(scriptName);
                    InspectorPanel.COMPONENTS.remove(script.getCompiledClass());
                    System.gc();
                    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
                    StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
                    try (MemoryFileManager mfm = new MemoryFileManager(standardFileManager, this.getClass().getClassLoader())) {
                        try {
                            ClassLoader cl = mfm.getClassLoader(StandardLocation.CLASS_OUTPUT);
                            engine.setExecutionClassLoader(cl);
                            JavaCompiledScript newScript = reloadScript(file);
                            Class<?> newScriptClass = newScript.getCompiledClass();
                            try {
                                Constructor<?> constructor = newScriptClass.getConstructor(GameObject.class);
                                Debug.log(constructor);
                                for (GameObject go : reloadedGameobjects) {
                                    Component component = (Component) constructor.newInstance(go);
                                    go.addComponent(component);
                                }
                                awaitingCompilation.remove(file.getName());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    reloadScript(file);
                }
            } else {
                if (script != null) {
                    Application.getApp().getCurrentScene().forEachGameObject((go) ->
                            go.removeComponent((Class<? extends Component>) script.getCompiledClass())
                    );
                    compiledScripts.remove(scriptName);
                    InspectorPanel.COMPONENTS.remove(script.getCompiledClass());
                }
            }
        }
    }
}
