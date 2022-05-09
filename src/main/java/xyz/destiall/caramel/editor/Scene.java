package xyz.destiall.caramel.editor;

import imgui.ImGui;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.Light;
import xyz.destiall.caramel.api.components.MeshRenderer;
import xyz.destiall.caramel.api.components.Script;
import xyz.destiall.caramel.editor.ui.*;
import xyz.destiall.caramel.api.mesh.Mesh;
import xyz.destiall.caramel.api.mesh.MeshBuilder;
import xyz.destiall.caramel.graphics.Texture;
import xyz.destiall.caramel.interfaces.Update;
import xyz.destiall.caramel.api.GameObject;

import java.util.*;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.opengl.GL11.*;

public class Scene implements Update {
    public static final String SCENE_DRAG_DROP_PAYLOAD = "SceneDragDropPayloadGameObject";

    private final List<GameObject> gameObjects;
    private final List<GameObject> defaultGameObjects;
    private final List<Panel> panels;

    private final HashMap<GameObject, GameObject> toAdd;
    private Set<Light> lights;
    private EditorCamera editorCamera;
    private Gizmo gizmo;
    private Camera gameCamera;

    private boolean playing = false;

    public GameObject selectedGameObject;
    public GameObject selectedPlayingGameObject;
    public GameObject hoveredGameObject;
    public String name;

    public Scene() {
        name = "Untitled Scene";
        gameObjects = new LinkedList<>();
        defaultGameObjects = new LinkedList<>();
        toAdd = new HashMap<>();
        panels = new ArrayList<>();
        panels.add(new HierarchyPanel(this));
        panels.add(new InspectorPanel(this));
        panels.add(new MenuBarPanel(this));
        panels.add(new ConsolePanel(this));
        panels.add(new GamePanel(this));
    }

    public <P extends Panel> P getEditorPanel(Class<P> clazz) {
        return clazz.cast(panels.stream().filter(p -> p.getClass().isAssignableFrom(clazz)).findFirst().orElse(null));
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    @Override
    public void update() {
        editorCamera.update();
        lights = gameObjects.stream().map(g -> g.getComponentsInChildren(Light.class)).reduce(new HashSet<>(), (s, l) -> {
            s.addAll(l);
            return s;
        });
        glEnable(GL_DEPTH_TEST);
        if (playing) {
            for (GameObject go : gameObjects) go.update();
            for (GameObject go : gameObjects) go.render();

        } else {
            for (GameObject go : gameObjects) {
                go.editorUpdate();
                if (gameCamera == null && go.getComponentInChildren(Camera.class) != null) {
                    gameCamera = go.getComponentInChildren(Camera.class);
                }
            }

            if (Input.isKeyDown(GLFW_KEY_G)) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            else glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            for (GameObject go : gameObjects) go.render();

            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            gizmo.setTarget(selectedGameObject);
        }
    }

    public void endFrame() {
        for (Map.Entry<GameObject, GameObject> entry : toAdd.entrySet()) {
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
                child.transform.localPosition.add(child.transform.position.sub(parent.transform.position, new Vector3f()));
            }
        }

        toAdd.clear();
    }

    @Override
    public void editorUpdate() {
       update();
    }

    public void play() {
        if (playing) return;
        playing = true;
        ConsolePanel.LOGS.clear();
        for (GameObject go : gameObjects) {
            GameObject clone = go.clone();
            defaultGameObjects.add(clone);
            if (selectedGameObject == go) {
                selectedPlayingGameObject = clone;
            }
        }
    }

    public void stop() {
        if (!playing) return;
        playing = false;
        gameObjects.clear();
        selectedGameObject = selectedPlayingGameObject;
        selectedPlayingGameObject = null;
        gameObjects.addAll(defaultGameObjects);
        defaultGameObjects.clear();
    }

    @Override
    public void imguiLayer() {
        for (Panel panel : panels) {
            panel.imguiLayer();
        }
        ImGui.showDemoWindow();
    }

    public void init() {
        GameObject go = new GameObject(this);
        go.addComponent(new EditorCamera(go));
        editorCamera = go.getComponent(EditorCamera.class);

        gizmo = new Gizmo();
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
    }

    public GameObject getSelectedGameObject() {
        return selectedGameObject;
    }

    public Set<Light> getLights() {
        return lights;
    }

    public void addGameObject(GameObject gameObject) {
        toAdd.put(null, gameObject);
    }

    public void addGameObject(GameObject gameObject, GameObject parent) {
        toAdd.put(parent, gameObject);
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
}
