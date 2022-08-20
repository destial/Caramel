package caramel.api.objects;

import caramel.api.Component;
import caramel.api.components.Camera;
import caramel.api.components.Light;
import caramel.api.interfaces.Render;
import caramel.api.interfaces.Update;
import caramel.api.utils.Pair;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This {@link Scene} represents a collection of {@link GameObject}s.
 */
public abstract class Scene implements Update, Render {
    public final AtomicInteger entityIds = new AtomicInteger(0);
    protected List<GameObject> gameObjects;
    protected List<Prefab> prefabs;
    protected List<GameObject> defaultGameObjects;
    protected List<Pair<GameObject, GameObject>> toAdd;
    protected Set<Light> lights;
    protected Set<Camera> gameCameras;
    protected File file;
    protected boolean playing = false;

    protected final Set<GameObject> selectedGameObject;
    protected final Set<GameObject> selectedDefaultGameObject;
    public String name;

    public Scene() {
        selectedGameObject = ConcurrentHashMap.newKeySet();
        selectedDefaultGameObject = ConcurrentHashMap.newKeySet();
        gameCameras = ConcurrentHashMap.newKeySet();
    }

    /**
     * Get every root {@link GameObject} currently in this {@link Scene}.
     * @return All root {@link GameObject}.
     */
    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    /**
     * Get every {@link Prefab} which are loaded in this {@link Scene}.
     * @return All {@link Prefab}.
     */
    public List<Prefab> getPrefabs() {
        return prefabs;
    }

    /**
     * Generates a unique entity ID for {@link Component}s and/or {@link GameObject}s.
     * @return A unique ID.
     */
    public int generateId() {
        return entityIds.incrementAndGet();
    }

    /**
     * Destroy a {@link GameObject} that is present in this {@link Scene}.
     * @param gameObject The {@link GameObject} to destroy.
     */
    public abstract void destroy(GameObject gameObject);

    /**
     * Get the file which this {@link Scene} was loaded from.
     * @return The origin file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the file to which this {@link Scene} will be saved to.
     * @param file The scene save file.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Whether this {@link Scene} is currently playing in editor mode.
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Get every selected {@link GameObject} in this {@link Scene}.
     * @return All selected {@link GameObject}.
     */
    public Set<GameObject> getSelectedGameObject() {
        return selectedGameObject;
    }

    /**
     * Add a game {@link Camera} to render the {@link Scene} to.
     * @param camera The game {@link Camera}.
     */
    public void addGameCamera(Camera camera) {
        gameCameras.add(camera);
    }

    /**
     * Remove a game {@link Camera} from the {@link Scene} to.
     * @param camera The game {@link Camera}.
     */
    public void removeGameCamera(Camera camera) {
        gameCameras.remove(camera);
    }

    /**
     * Get the game {@link Camera}s that the {@link Scene} is rendering to.
     * @return The game {@link Camera}s.
     */
    public Collection<Camera> getGameCameras() {
        return gameCameras;
    }

    /**
     * Get the main editor {@link Camera} that the {@link Scene} is rendering to.
     * @return The editor {@link Camera}.
     */
    public abstract Camera getEditorCamera();

    /**
     * Find a {@link GameObject} that matches this name in this {@link Scene}.
     * @param name The name of the {@link GameObject} to find.
     * @return The matching {@link GameObject}, null if none found.
     */
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

    /**
     * Get every {@link Light} components in this {@link Scene}.
     * @return All {@link Light} components.
     */
    public Set<Light> getLights() {
        return lights;
    }

    /**
     * Add a {@link GameObject} to this {@link Scene}.
     * @param gameObject The {@link GameObject} to add.
     */
    public void addGameObject(GameObject gameObject) {
        toAdd.add(new Pair<>(null, gameObject));
    }

    /**
     * Add a {@link GameObject} to a parent {@link GameObject} in this {@link Scene}.
     * @param gameObject The {@link GameObject} to add.
     * @param parent The parent {@link GameObject}.
     */
    public void addGameObject(GameObject gameObject, GameObject parent) {
        toAdd.add(new Pair<>(parent, gameObject));
    }

    /**
     * Loop through every {@link GameObject}, including children, in this {@link Scene}.
     * @param func The function to run for every {@link GameObject}.
     */
    public void forEachGameObject(Consumer<GameObject> func) {
        forEachGameObject(gameObjects, func);
    }

    private void forEachGameObject(List<GameObject> objects, Consumer<GameObject> func) {
        for (GameObject gameObject : objects) {
            if (gameObject.children.size() > 0) forEachGameObject(gameObject.children, func);
            func.accept(gameObject);
        }
    }

    /**
     * Check if a {@link Component} or {@link GameObject} exists with this ID in this {@link Scene}.
     * @param id The ID to check.
     * @return true if already existing, else false.
     */
    public boolean entityIdExists(int id) {
        return entityIdExists(getGameObjects(), id);
    }

    private boolean entityIdExists(List<GameObject> list, int id) {
        return list.stream().anyMatch(g -> g.id == id || g.getMutableComponents().stream().anyMatch(c -> c.id == id) || entityIdExists(g.children, id));
    }
}
