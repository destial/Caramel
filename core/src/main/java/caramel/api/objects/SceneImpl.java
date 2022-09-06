package caramel.api.objects;

import caramel.api.Application;
import caramel.api.Component;
import caramel.api.Input;
import caramel.api.components.Camera;
import caramel.api.components.EditorCamera;
import caramel.api.components.Light;
import caramel.api.components.UICamera;
import caramel.api.debug.Debug;
import caramel.api.debug.DebugImpl;
import caramel.api.events.ScenePlayEvent;
import caramel.api.events.SceneStopEvent;
import caramel.api.graphics.Graphics;
import caramel.api.render.BatchRenderer;
import caramel.api.scripts.Script;
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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static caramel.api.graphics.GL20.GL_DEPTH_TEST;
import static caramel.api.graphics.GL20.GL_FILL;
import static caramel.api.graphics.GL20.GL_FRONT_AND_BACK;
import static caramel.api.graphics.GL20.GL_LINE;

public final class SceneImpl extends Scene {
    private final Vector3f selectionColor = new Vector3f(1, 0, 0);

    private final Map<Physics.Mode, Physics> physics;
    private final Map<Class<?>, Panel> panels;
    private final List<EditorAction> actions;
    private final List<EditorAction> redoActions;
    private final UICamera uiCamera;

    private boolean saved = true;
    private EditorCamera editorCamera;

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
            panels.put(NodePanel.class, new NodePanel(this));
        }

        final GameObjectImpl go = new GameObjectImpl(this);
        go.addComponent(new EditorCamera(go));
        editorCamera = go.getComponent(EditorCamera.class);
        physics = new HashMap<>();
        physics.put(Physics.Mode._2D, new Physics2D(this));
        physics.put(Physics.Mode._3D, new Physics3D(this));

        final GameObject ui = new GameObjectImpl(this);
        uiCamera = new UICamera(ui);
        ui.addComponent(uiCamera);
    }

    public <P extends Panel> P getEditorPanel(final Class<P> clazz) {
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

        for (final GameObject go : gameObjects) go.update();
        for (final Physics world : physics.values()) {
            world.update();
        }
        for (final GameObject go : gameObjects) go.lateUpdate();

        for (final GameObject go : gameObjects) {
            go.editorUpdate();
            if (go.getComponentInChildren(Camera.class) != null) {
                gameCameras.add(go.getComponentInChildren(Camera.class));
            }
        }
    }

    public void undoLastAction() {
        if (actions.size() == 0) return;
        final EditorAction action = actions.get(actions.size() - 1);
        action.undo();
        actions.remove(action);
        redoActions.add(action);
    }

    public void redoLastAction() {
        if (redoActions.size() == 0) return;
        final EditorAction action = redoActions.get(redoActions.size() - 1);
        action.redo();
        redoActions.remove(action);
        actions.add(action);
    }

    public void addUndoAction(final EditorAction action) {
        actions.add(action);
        redoActions.clear();
        saved = false;
    }

    public void setSaved(final boolean saved) {
        this.saved = saved;
    }

    public boolean isSaved() {
        return saved;
    }

    @Override
    public void render(final Camera camera) {
        Graphics.get().glEnable(GL_DEPTH_TEST);
        if (playing) {
            for (final GameObject go : gameObjects) go.render(camera);

            if (BatchRenderer.USE_BATCH) BatchRenderer.render(camera);
        } else {
            if (ImGui.isKeyDown(Input.Key.G)) Graphics.get().glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            else Graphics.get().glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            for (final GameObject go : gameObjects) go.render(camera);

            if (BatchRenderer.USE_BATCH) BatchRenderer.render(camera);

            Graphics.get().glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

    }

    public UICamera getUICamera() {
        return uiCamera;
    }

    public boolean canUndo() {
        return !actions.isEmpty();
    }

    public boolean canRedo() {
        return !redoActions.isEmpty();
    }

    public void endFrame() {
        for (final Pair<GameObject, GameObject> entry : toAdd) {
            final GameObject parent = entry.getKey();
            final GameObject child = entry.getValue();
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

            if (playing) {
                for (final Physics world : physics.values()) {
                    world.addGameObject(child);
                }
            }
        }

        toAdd.clear();
    }

    @Override
    public void editorUpdate() {
        editorCamera.gameObject.update();

        if (playing) {
            for (final GameObject go : gameObjects) {
                go.update();
                if (go.getComponentInChildren(Camera.class) != null) {
                    gameCameras.add(go.getComponentInChildren(Camera.class));
                }
            }
            for (final Physics world : physics.values()) {
                world.update();
            }
            for (final GameObject go : gameObjects) go.lateUpdate();

        } else {
            if (ImGui.getIO().getKeyCtrl() && ImGui.isKeyPressed(Input.Key.Z)) {
                undoLastAction();
            }

            if (ImGui.getIO().getKeyCtrl() && ImGui.isKeyPressed(Input.Key.Y)) {
                redoLastAction();
            }
        }

        for (final GameObject go : gameObjects) {
            go.editorUpdate();
            if (go.getComponentInChildren(Camera.class) != null) {
                gameCameras.add(go.getComponentInChildren(Camera.class));
            }
        }

        editorCamera.gameObject.lateUpdate();

        for (final GameObject selected : selectedGameObject) {
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
        for (final GameObject go : defaultGameObjects) {
            final GameObject clone = go.clone(true);
            gameObjects.add(clone);
            if (selectedDefaultGameObject.contains(go)) {
                selectedGameObject.add(clone);
            }
            for (final Physics world : physics.values()) {
                try {
                    world.addGameObject(clone);
                } catch (Exception e) {
                    Debug.log(e.getMessage());
                }
            }
        }

        for (final GameObject clone : gameObjects) {
            for (final Component component : clone.getMutableComponents()) {
                if (component instanceof Script) {
                    try {
                        for (final Field field : component.getClass().getDeclaredFields()) {
                            if (Modifier.isTransient(field.getModifiers()) || !Component.class.isAssignableFrom(field.getType())) continue;
                            field.setAccessible(true);
                            final Component value = (Component) field.get(component);
                            if (value != null) {
                                for (final GameObject def : defaultGameObjects) {
                                    boolean b = false;
                                    for (final Component c : def.getMutableComponents()) {
                                        if (value == c) {
                                            final Component clonedComponent = gameObjects.stream().filter(g -> g.id == def.id).findFirst().get().getComponent((Class<? extends Component>) field.getType());
                                            field.set(component, clonedComponent);
                                            b = true;
                                            break;
                                        }
                                    }
                                    if (b) break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Debug.log(e.getMessage());
                    }
                }
            }
        }

        gameCameras.clear();
        Application.getApp().getEventHandler().call(new ScenePlayEvent(this));
    }

    public void stop() {
        if (!playing) return;
        Application.getApp().getEventHandler().call(new SceneStopEvent(this));

        playing = false;
        gameObjects.clear();
        gameObjects.addAll(defaultGameObjects);

        selectedGameObject.clear();
        selectedGameObject.addAll(selectedDefaultGameObject);

        defaultGameObjects.clear();
        selectedDefaultGameObject.clear();

        for (final Physics p : physics.values()) {
            p.reset();
        }
        gameCameras.clear();
    }

    public void __imguiLayer() {
        for (final Panel panel : panels.values()) {
            try {
                panel.__imguiLayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setEditorCamera(final EditorCamera camera) {
        editorCamera = camera;
    }

    public EditorCamera getEditorCamera() {
        return editorCamera;
    }

    public void destroy(final GameObject gameObject) {
        if (gameObject.parent != null) {
            gameObject.parent.gameObject.children.remove(gameObject);
        } else {
            gameObjects.remove(gameObject);
        }
        Camera camera;
        if ((camera = gameObject.getComponentInChildren(Camera.class)) != null) {
            gameCameras.remove(camera);
        }
        if ((camera = gameObject.getComponent(Camera.class)) != null) {
            gameCameras.remove(camera);
        }
        selectedGameObject.remove(gameObject);
        selectedDefaultGameObject.remove(gameObject);
        for (final Physics world : physics.values()) {
            world.removeGameObject(gameObject);
        }
    }

    public void invalidate() {
        for (final Physics physics : physics.values()) {
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
