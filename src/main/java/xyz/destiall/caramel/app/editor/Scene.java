package xyz.destiall.caramel.app.editor;

import imgui.ImGui;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.Light;
import xyz.destiall.caramel.api.objects.Prefab;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.app.editor.debug.DebugDraw;
import xyz.destiall.caramel.app.editor.ui.GamePanel;
import xyz.destiall.caramel.app.editor.ui.InspectorPanel;
import xyz.destiall.caramel.app.editor.ui.MenuBarPanel;
import xyz.destiall.caramel.app.editor.ui.ConsolePanel;
import xyz.destiall.caramel.app.editor.ui.ScenePanel;
import xyz.destiall.caramel.app.editor.ui.HierarchyPanel;
import xyz.destiall.caramel.app.editor.ui.Panel;
import xyz.destiall.caramel.app.physics.Physics;
import xyz.destiall.caramel.app.physics.Physics2D;
import xyz.destiall.caramel.app.physics.Physics3D;
import xyz.destiall.caramel.app.utils.Pair;
import xyz.destiall.caramel.api.interfaces.Render;
import xyz.destiall.caramel.api.interfaces.Update;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPolygonMode;

public class Scene implements Update, Render {
    public static final String SCENE_DRAG_DROP_PAYLOAD = "SceneDragDropPayloadGameObject";

    private final List<GameObject> gameObjects;
    private final List<Prefab> prefabs;
    private final List<GameObject> defaultGameObjects;
    private final Map<Class<?>, Panel> panels;
    private final Map<Physics.Mode, Physics> physics;
    private final List<Pair<GameObject, GameObject>> toAdd;

    private Set<Light> lights;

    private EditorCamera editorCamera;
    private Gizmo gizmo;
    private Camera gameCamera;

    private File file;

    private boolean playing = false;
    private boolean saved = true;
    private Physics.Mode physicsMode = Physics.Mode._2D;

    public GameObject selectedGameObject;
    public GameObject selectedPlayingGameObject;
    public GameObject hoveredGameObject;
    public String name;

    public Scene() {
        name = "Untitled Scene";
        file = new File("assets/scenes", name + ".json");
        gameObjects = new LinkedList<>();
        prefabs = new LinkedList<>();
        defaultGameObjects = new LinkedList<>();
        toAdd = new ArrayList<>();
        panels = new HashMap<>();
        if (Application.getApp().EDITOR_MODE) {
            panels.put(HierarchyPanel.class, new HierarchyPanel(this));
            panels.put(InspectorPanel.class, new InspectorPanel(this));
            panels.put(MenuBarPanel.class, new MenuBarPanel(this));
            panels.put(ConsolePanel.class, new ConsolePanel(this));
            panels.put(ScenePanel.class, new ScenePanel(this));
            panels.put(GamePanel.class, new GamePanel(this));
        }
        gizmo = new Gizmo();

        GameObject go = new GameObject(this);
        go.addComponent(new EditorCamera(go));
        editorCamera = go.getComponent(EditorCamera.class);
        physics = new HashMap<>();
        physics.put(Physics.Mode._2D, new Physics2D(this));
        physics.put(Physics.Mode._3D, new Physics3D(this));
    }

    public <P extends Panel> P getEditorPanel(Class<P> clazz) {
        return clazz.cast(panels.get(clazz));
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public List<Prefab> getPrefabs() {
        return prefabs;
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

    @Override
    public void render(Camera camera) {
        glEnable(GL_DEPTH_TEST);
        if (playing) {
            for (GameObject go : gameObjects) go.render(camera);
        } else {
            if (Input.isKeyDown(GLFW_KEY_G)) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            else glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            for (GameObject go : gameObjects) go.render(camera);

            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            gizmo.render(camera);
        }
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
                child.transform.localPosition.add(child.transform.position.sub(parent.transform.position, new Vector3f()));
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
            gizmo.setTarget(selectedGameObject);
        }
        editorCamera.gameObject.lateUpdate();

        if (selectedGameObject != null) {
            Debug.drawBox2D(selectedGameObject.transform.position, selectedGameObject.transform.scale, new Vector3f(1, 0, 0));
        }
        DebugDraw.INSTANCE.update();
    }

    public void play() {
        if (playing) return;
        playing = true;
        ConsolePanel.LOGS.clear();
        for (GameObject go : gameObjects) {
            if (Application.getApp().EDITOR_MODE) {
                GameObject clone = go.clone();
                defaultGameObjects.add(clone);
                if (selectedGameObject == go) {
                    selectedPlayingGameObject = clone;
                }
            }
            physics.get(physicsMode).addGameObject(go);
        }
    }

    public void stop() {
        if (!playing) return;
        playing = false;
        if (Application.getApp().EDITOR_MODE) {
            gameObjects.clear();
            selectedGameObject = selectedPlayingGameObject;
            selectedPlayingGameObject = null;
            gameObjects.addAll(defaultGameObjects);
            defaultGameObjects.clear();
            for (Physics p : physics.values()) {
                p.reset();
            }
        }
    }

    @Override
    public void imguiLayer() {
        for (Panel panel : panels.values()) {
            panel.imguiLayer();
        }
        ImGui.showDemoWindow();
    }

    public void setEditorCamera(EditorCamera camera) {
        editorCamera = camera;
    }

    public void setGameCamera(Camera camera) {
        this.gameCamera = camera;
    }

    public GameObject findGameObject(String name) {
        return findGameObject(gameObjects, name);
    }

    private GameObject findGameObject(List<GameObject> children, String name) {
        for (GameObject child : children) {
            if (child.name.equals(name)) return child;
            GameObject o = findGameObject(child.children, name);
            if (o != null) return o;
        }
        return null;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isPlaying() {
        return playing;
    }

    public EditorCamera getEditorCamera() {
        return editorCamera;
    }

    public Camera getGameCamera() {
        return gameCamera;
    }

    public void destroy(GameObject gameObject) {
        if (gameObject.parent != null) {
            gameObject.parent.gameObject.children.remove(gameObject);
        } else {
            gameObjects.remove(gameObject);
        }
        Camera camera;
        if ((camera = gameObject.getComponentInChildren(Camera.class)) != null) {
            if (camera == gameCamera) gameCamera = null;
        }
        physics.get(physicsMode).removeGameObject(gameObject);
    }

    public GameObject getSelectedGameObject() {
        return selectedGameObject;
    }

    public Set<Light> getLights() {
        return lights;
    }

    public void addGameObject(GameObject gameObject) {
        toAdd.add(new Pair<>(null, gameObject));
    }

    public void addGameObject(GameObject gameObject, GameObject parent) {
        toAdd.add(new Pair<>(parent, gameObject));
    }

    public void forEachGameObject(Consumer<GameObject> func) {
        forEachGameObject(gameObjects, func);
    }

    private void forEachGameObject(List<GameObject> objects, Consumer<GameObject> func) {
        for (GameObject gameObject : objects) {
            if (gameObject.children.size() > 0) forEachGameObject(gameObject.children, func);
            func.accept(gameObject);
        }
    }

    public boolean entityIdExists(int id) {
        return entityIdExists(getGameObjects(), id);

    }

    private boolean entityIdExists(List<GameObject> list, int id) {
        return list.stream().anyMatch(g -> g.id == id || g.getComponents().stream().anyMatch(c -> c.id == id) || entityIdExists(g.children, id));
    }
}
