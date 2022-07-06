package xyz.destiall.caramel.api.objects;

import org.joml.Vector3f;
import xyz.destiall.caramel.api.Application;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.EditorCamera;
import xyz.destiall.caramel.api.debug.DebugImpl;
import xyz.destiall.caramel.api.events.ScenePlayEvent;
import xyz.destiall.caramel.api.events.SceneStopEvent;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.objects.GameObjectImpl;
import xyz.destiall.caramel.api.components.Light;
import xyz.destiall.caramel.api.objects.Scene;
import xyz.destiall.caramel.app.ApplicationImpl;
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
import xyz.destiall.caramel.api.utils.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPolygonMode;

public final class SceneImpl extends Scene {
    private final Map<Physics.Mode, Physics> physics;
    private final Map<Class<?>, Panel> panels;

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

        if (ApplicationImpl.getApp().EDITOR_MODE) {
            panels.put(HierarchyPanel.class, new HierarchyPanel(this));
            panels.put(InspectorPanel.class, new InspectorPanel(this));
            panels.put(MenuBarPanel.class, new MenuBarPanel(this));
            panels.put(ConsolePanel.class, new ConsolePanel(this));
            panels.put(ScenePanel.class, new ScenePanel(this));
            panels.put(GamePanel.class, new GamePanel(this));
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
                // child.transform.position.set(parent.transform.position.sub(child.transform.position, new Vector3f()));
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
        }
        editorCamera.gameObject.lateUpdate();

        for (GameObject selected : selectedGameObject) {
            DebugImpl.drawBox2D(selected.transform.position, selected.transform.scale, new Vector3f(1, 0, 0));
        }

        DebugDraw.INSTANCE.update();
    }

    public void play() {
        if (playing) return;
        playing = true;
        ConsolePanel.LOGS.clear();
        for (GameObject go : gameObjects) {
            if (ApplicationImpl.getApp().EDITOR_MODE) {
                GameObject clone = go.clone(true);
                defaultGameObjects.add(clone);

                if (selectedGameObject.contains(go)) {
                    selectedPlayingGameObject.add(go);
                }
            }
            physics.get(physicsMode).addGameObject(go);
        }

        selectedGameObject.clear();
        Application.getApp().getEventHandler().call(new ScenePlayEvent(this));
    }

    public void stop() {
        if (!playing) return;
        playing = false;
        if (ApplicationImpl.getApp().EDITOR_MODE) {
            selectedGameObject.clear();
            selectedGameObject.addAll(selectedPlayingGameObject);
            selectedPlayingGameObject.clear();

            gameObjects.clear();
            gameObjects.addAll(defaultGameObjects);
            defaultGameObjects.clear();
            for (Physics p : physics.values()) {
                p.reset();
            }
        }
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

    @Override
    public int getSceneViewX() {
        return (int) getEditorPanel(ScenePanel.class).getWindowSize().x;
    }

    @Override
    public int getSceneViewY() {
        return (int) getEditorPanel(ScenePanel.class).getWindowSize().y;
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
        selectedGameObject.remove(gameObject);
        selectedPlayingGameObject.remove(gameObject);
        physics.get(physicsMode).removeGameObject(gameObject);
    }
}
