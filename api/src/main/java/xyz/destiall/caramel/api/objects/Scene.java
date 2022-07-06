package xyz.destiall.caramel.api.objects;

import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.Light;
import xyz.destiall.caramel.api.interfaces.Render;
import xyz.destiall.caramel.api.interfaces.Update;
import xyz.destiall.caramel.api.utils.Pair;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class Scene implements Update, Render {
    public final AtomicInteger entityIds = new AtomicInteger(0);
    protected List<GameObject> gameObjects;
    protected List<Prefab> prefabs;
    protected List<GameObject> defaultGameObjects;
    protected List<Pair<GameObject, GameObject>> toAdd;
    protected Set<Light> lights;
    protected Camera gameCamera;
    protected File file;
    protected boolean playing = false;
    protected boolean saved = true;

    protected final Set<GameObject> selectedGameObject;
    protected final Set<GameObject> selectedPlayingGameObject;
    public GameObject hoveredGameObject;
    public String name;

    public Scene() {
        selectedGameObject = ConcurrentHashMap.newKeySet();
        selectedPlayingGameObject = ConcurrentHashMap.newKeySet();
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

    public int generateId() {
        return entityIds.incrementAndGet();
    }

    public abstract void destroy(GameObject gameObject);

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isPlaying() {
        return playing;
    }

    public Set<GameObject> getSelectedGameObject() {
        return isPlaying() ? selectedPlayingGameObject : selectedGameObject;
    }

    public void setGameCamera(Camera camera) {
        this.gameCamera = camera;
    }

    public Camera getGameCamera() {
        return gameCamera;
    }

    public abstract Camera getEditorCamera();

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
        return list.stream().anyMatch(g -> g.id == id || g.getMutableComponents().stream().anyMatch(c -> c.id == id) || entityIdExists(g.children, id));
    }

    public abstract int getSceneViewX();

    public abstract int getSceneViewY();
}
