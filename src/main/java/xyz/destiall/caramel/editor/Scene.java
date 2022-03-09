package xyz.destiall.caramel.editor;

import imgui.ImGui;
import org.joml.Vector3f;
import xyz.destiall.caramel.app.input.Input;
import xyz.destiall.caramel.components.Camera;
import xyz.destiall.caramel.components.MeshRenderer;
import xyz.destiall.caramel.components.Script;
import xyz.destiall.caramel.editor.ui.HierarchyPanel;
import xyz.destiall.caramel.editor.ui.InspectorPanel;
import xyz.destiall.caramel.editor.ui.MenuBarPanel;
import xyz.destiall.caramel.editor.ui.Panel;
import xyz.destiall.caramel.graphics.Mesh;
import xyz.destiall.caramel.graphics.MeshBuilder;
import xyz.destiall.caramel.graphics.Texture;
import xyz.destiall.caramel.interfaces.Update;
import xyz.destiall.caramel.objects.GameObject;

import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.opengl.GL11.*;

public class Scene implements Update {
    public static final String SCENE_DRAG_DROP_PAYLOAD = "SceneDragDropPayloadGameObject";


    private final List<GameObject> gameObjects;
    private final List<GameObject> defaultGameObjects;
    private final List<Panel> panels;

    private final HashMap<GameObject, GameObject> toAdd;
    private EditorCamera editorCamera;
    private Gizmo gizmo;
    private Camera gameCamera;

    private boolean playing = false;

    public GameObject selectedGameObject;
    public GameObject selectedPlayingGameObject;
    public GameObject hoveredGameObject;

    public Scene() {
        gameObjects = new LinkedList<>();
        defaultGameObjects = new LinkedList<>();
        toAdd = new HashMap<>();
        panels = new ArrayList<>();
        panels.add(new HierarchyPanel(this));
        panels.add(new InspectorPanel(this));
        panels.add(new MenuBarPanel(this));
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    @Override
    public void update() {
        editorCamera.update();
        glEnable(GL_DEPTH_TEST);
        if (playing) {

            for (GameObject go : gameObjects) go.update();
            for (GameObject go : gameObjects) go.render();

        } else {

            for (GameObject go : gameObjects) {
                go.editorUpdate();
                if (gameCamera == null && go.hasComponent(Camera.class)) {
                    gameCamera = go.getComponent(Camera.class);
                }
            }

            if (Input.isKeyDown(GLFW_KEY_G)) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            else                             glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

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
        Texture floosh = new Texture("assets/textures/floosh-wide.png");

        GameObject go = new GameObject(this);
        go.transform.position.add(0, 0, -11);
        go.addComponent(new MeshRenderer(go));
        go.addComponent(new Script(go));
        Mesh mesh = MeshBuilder.createModel("assets/models/sphere.obj");
        mesh.setTexture(floosh);
        mesh.build();
        go.getComponent(MeshRenderer.class).setMesh(mesh);
        gameObjects.add(go);

        go = new GameObject(this);
        go.transform.position.zero();
        go.addComponent(new MeshRenderer(go));
        go.name = "Axes";
        mesh = MeshBuilder.createAxes(1);
        mesh.build();
        go.getComponent(MeshRenderer.class).setMesh(mesh);
        gameObjects.add(go);

        selectedGameObject = gameObjects.get(0);

        go = new GameObject(this);
        go.addComponent(new EditorCamera(go));
        editorCamera = go.getComponent(EditorCamera.class);

        gizmo = new Gizmo();
    }

    public GameObject findGameObject(String name) {
        for (GameObject go : gameObjects) {
            if (go.name.equals(name)) return go;
        }
        return null;
    }

    public boolean isPlaying() {
        return playing;
    }

    public EditorCamera getMainCamera() {
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

    public void addGameObject(GameObject gameObject) {
        toAdd.put(null, gameObject);
    }

    public void addGameObject(GameObject gameObject, GameObject parent) {
        toAdd.put(parent, gameObject);
    }
}
