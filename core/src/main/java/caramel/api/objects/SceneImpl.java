package caramel.api.objects;

import caramel.api.Application;
import caramel.api.Input;
import caramel.api.components.Camera;
import caramel.api.components.EditorCamera;
import caramel.api.components.Light;
import caramel.api.debug.DebugImpl;
import caramel.api.events.ScenePlayEvent;
import caramel.api.events.SceneStopEvent;
import caramel.api.render.BatchRenderer;
import caramel.api.utils.Pair;
import imgui.ImGui;
import org.joml.Vector3f;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.editor.action.EditorAction;
import xyz.destiall.caramel.app.editor.debug.DebugDraw;
import xyz.destiall.caramel.app.editor.panels.ConsolePanel;
import xyz.destiall.caramel.app.editor.panels.GamePanel;
import xyz.destiall.caramel.app.editor.panels.HierarchyPanel;
import xyz.destiall.caramel.app.editor.panels.InspectorPanel;
import xyz.destiall.caramel.app.editor.panels.MenuBarPanel;
import xyz.destiall.caramel.app.editor.panels.NodePanel;
import xyz.destiall.caramel.app.editor.panels.Panel;
import xyz.destiall.caramel.app.editor.panels.ScenePanel;
import xyz.destiall.caramel.app.physics.Physics;
import xyz.destiall.caramel.app.physics.Physics2D;
import xyz.destiall.caramel.app.physics.Physics3D;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPolygonMode;

public final class SceneImpl extends Scene {
    private final Vector3f selectionColor = new Vector3f(1, 0, 0);

    private final Map<Physics.Mode, Physics> physics;
    private final Map<Class<?>, Panel> panels;
    private final List<EditorAction> actions;
    private final List<EditorAction> redoActions;
    private boolean saved = true;

    private EditorCamera editorCamera;
    private Physics.Mode physicsMode = Physics.Mode._2D;

    public SceneImpl() {
        name = "Untitled Scene";
        file = new File("assets/scenes", name + ".caramel");
        gameObjects = new LinkedList<>();
        prefabs = new LinkedList<>();
        defaultGameObjects = new LinkedList<>();
        toAdd = new ArrayList<>();
        panels = new HashMap<>();
        actions = new ArrayList<>();
        redoActions = new ArrayList<>();

        if (ApplicationImpl.getApp().EDITOR_MODE) {
            panels.put(HierarchyPanel.class, new HierarchyPanel(this));
            panels.put(InspectorPanel.class, new InspectorPanel(this));
            panels.put(MenuBarPanel.class, new MenuBarPanel(this));
            panels.put(ConsolePanel.class, new ConsolePanel(this));
            panels.put(ScenePanel.class, new ScenePanel(this));
            panels.put(GamePanel.class, new GamePanel(this));
            // panels.put(NodePanel.class, new NodePanel(this));
        }

        GameObjectImpl go = new GameObjectImpl(this);
        go.addComponent(new EditorCamera(go));
        editorCamera = go.getComponent(EditorCamera.class);
        physics = new HashMap<>();
        physics.put(Physics.Mode._2D, new Physics2D(this));
        physics.put(Physics.Mode._3D, new Physics3D(this));
    }

    public <P extends Panel> P getEditorPanel(Class<P> clazz) {
        return clazz.cast(panels.get(clazz));
    }

    @Override
    public void update() {
        lights = gameObjects.stream()
                .map(g -> g.getComponentsInChildren(Light.class))
                .reduce(new HashSet<>(), (s, l) -> {
                    s.addAll(l);
                    return s;
                });

        for (GameObject go : gameObjects) go.update();
        physics.get(physicsMode).update();
        for (GameObject go : gameObjects) go.lateUpdate();
    }

    public void undoLastAction() {
        if (actions.size() == 0) return;
        EditorAction action = actions.get(actions.size() - 1);
        action.undo();
        actions.remove(action);
        redoActions.add(action);
    }

    public void redoLastAction() {
        if (redoActions.size() == 0) return;
        EditorAction action = redoActions.get(redoActions.size() - 1);
        action.redo();
        redoActions.remove(action);
        actions.add(action);
    }

    public void addUndoAction(EditorAction action) {
        actions.add(action);
        redoActions.clear();
        saved = false;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isSaved() {
        return saved;
    }

    @Override
    public void render(Camera camera) {
        glEnable(GL_DEPTH_TEST);
        if (playing) {
            for (GameObject go : gameObjects) go.render(camera);

            if (BatchRenderer.USE_BATCH) BatchRenderer.render();
        } else {
            if (ImGui.isKeyDown(Input.Key.G)) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            else glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            for (GameObject go : gameObjects) go.render(camera);

            if (BatchRenderer.USE_BATCH) BatchRenderer.render();

            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

    }

    public boolean canUndo() {
        return !actions.isEmpty();
    }

    public boolean canRedo() {
        return !redoActions.isEmpty();
    }

    public void endFrame() {
        for (Pair<GameObject, GameObject> entry : toAdd) {
            GameObject parent = entry.getKey();
            GameObject child = entry.getValue();
            if (child.parent != null) {
                child.parent.gameObject.children.remove(child);
            } else {
                gameObjects.remove(child);
            }

            if (parent == null) {
                gameObjects.add(child);
            } else {
                parent.children.add(child);
                child.parent = parent.transform;
                child.scene = parent.scene;
                child.transform.localPosition.set(parent.transform.position.sub(child.transform.position, new Vector3f()));
                child.transform.position.set(parent.transform.position);
            }

            if (playing) physics.get(physicsMode).addGameObject(child);
        }

        toAdd.clear();
    }

    public void setPhysicsMode(Physics.Mode physicsMode) {
        if (!playing) this.physicsMode = physicsMode;
    }

    public Physics.Mode getPhysicsMode() {
        return physicsMode;
    }

    @Override
    public void editorUpdate() {
        if (ImGui.getIO().getKeyCtrl() && ImGui.isKeyPressed(Input.Key.Z)) {
            undoLastAction();
        }

        if (ImGui.getIO().getKeyCtrl() && ImGui.isKeyPressed(Input.Key.Y)) {
            redoLastAction();
        }

        editorCamera.gameObject.update();
        lights = gameObjects.stream()
                .map(g -> g.getComponentsInChildren(Light.class))
                .reduce(new HashSet<>(), (s, l) -> {
                    s.addAll(l);
                    return s;
                });

        if (playing) {
            for (GameObject go : gameObjects) go.update();
            physics.get(physicsMode).update();
            for (GameObject go : gameObjects) go.lateUpdate();

        } else {
            for (GameObject go : gameObjects) {
                go.editorUpdate();
                if (gameCamera == null && go.getComponentInChildren(Camera.class) != null) {
                    gameCamera = go.getComponentInChildren(Camera.class);
                }
            }
        }
        editorCamera.gameObject.lateUpdate();

        for (GameObject selected : selectedGameObject) {
            DebugImpl.drawOutline(selected.transform, selectionColor);
        }

        DebugDraw.INSTANCE.update();
    }

    public void play() {
        if (playing) return;
        playing = true;
        ConsolePanel.LOGS.clear();
        defaultGameObjects.addAll(gameObjects);
        selectedDefaultGameObject.addAll(selectedGameObject);

        gameObjects.clear();
        selectedGameObject.clear();
        for (GameObject go : defaultGameObjects) {
            GameObject clone = go.clone(true);
            gameObjects.add(clone);
            if (selectedDefaultGameObject.contains(go)) {
                selectedGameObject.add(clone);
            }
            physics.get(physicsMode).addGameObject(clone);
        }
        Application.getApp().getEventHandler().call(new ScenePlayEvent(this));
    }

    public void stop() {
        if (!playing) return;
        playing = false;
        gameObjects.clear();
        gameObjects.addAll(defaultGameObjects);

        selectedGameObject.clear();
        selectedGameObject.addAll(selectedDefaultGameObject);

        defaultGameObjects.clear();
        selectedDefaultGameObject.clear();

        for (Physics p : physics.values()) {
            p.reset();
        }
        gameCamera = null;

        Application.getApp().getEventHandler().call(new SceneStopEvent(this));
    }

    public void __imguiLayer() {
        for (Panel panel : panels.values()) {
            panel.__imguiLayer();
        }
    }

    public void setEditorCamera(EditorCamera camera) {
        editorCamera = camera;
    }

    public EditorCamera getEditorCamera() {
        return editorCamera;
    }

    public void destroy(GameObject gameObject) {
        if (gameObject.parent != null) {
            gameObject.parent.gameObject.children.remove(gameObject);
        } else {
            gameObjects.remove(gameObject);
        }
        Camera camera;
        if ((camera = gameObject.getComponentInChildren(Camera.class)) != null) {
            if (camera == gameCamera) {
                gameCamera = null;
            }
        }
        if ((camera = gameObject.getComponent(Camera.class)) != null) {
            if (camera == gameCamera) {
                gameCamera = null;
            }
        }
        selectedGameObject.remove(gameObject);
        selectedDefaultGameObject.remove(gameObject);
        physics.get(physicsMode).removeGameObject(gameObject);
    }

    public void invalidate() {
        for (Physics physics : physics.values()) {
            physics.invalidate();
        }
        physics.clear();
        gameObjects.clear();
        defaultGameObjects.clear();
        selectedDefaultGameObject.clear();
        selectedGameObject.clear();
        actions.clear();
        redoActions.clear();
        panels.clear();
        toAdd.clear();
        prefabs.clear();
        editorCamera = null;
    }
}
