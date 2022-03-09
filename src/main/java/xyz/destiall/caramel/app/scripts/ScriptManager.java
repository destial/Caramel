package xyz.destiall.caramel.app.scripts;

import ch.obermuhlner.scriptengine.java.JavaCompiledScript;
import ch.obermuhlner.scriptengine.java.JavaScriptEngine;
import ch.obermuhlner.scriptengine.java.JavaScriptEngineFactory;
import ch.obermuhlner.scriptengine.java.constructor.NullConstructorStrategy;
import xyz.destiall.caramel.app.Debug;
import xyz.destiall.caramel.components.Component;
import xyz.destiall.caramel.editor.ui.InspectorPanel;
import xyz.destiall.caramel.objects.GameObject;
import xyz.destiall.java.reflection.Reflect;

import javax.script.ScriptException;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

public class ScriptManager {
    private final File scriptsRootFolder;
    private final JavaScriptEngine engine;
    private final HashMap<String, JavaCompiledScript> compiledScripts;

    public ScriptManager() {
        scriptsRootFolder = new File("assets/scripts/");
        scriptsRootFolder.mkdir();
        engine = (JavaScriptEngine) new JavaScriptEngineFactory().getScriptEngine();
        engine.setConstructorStrategy((clazz) -> new NullConstructorStrategy());
        compiledScripts = new HashMap<>();
    }

    public void reloadAll() {
        if (!scriptsRootFolder.exists()) scriptsRootFolder.mkdir();
        loadScripts(scriptsRootFolder);
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

    public void reloadScript(File file) {
        if (file.getName().endsWith(".java")) {
            String scriptName = file.getName().substring(0, file.getName().length() - ".java".length());
            if (compiledScripts.containsKey(scriptName)) return;
            String contents = readScript(file);
            try {
                JavaCompiledScript compiledScript = engine.compile(contents);
                if (compiledScript.getCompiledClass().isAssignableFrom(Component.class)) {
                    Debug.logError("Script " + scriptName + " does not inherit Component class!");
                    return;
                }
                compiledScripts.put(scriptName, compiledScript);
                InspectorPanel.COMPONENTS.add(compiledScript.getCompiledClass());
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
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
}
