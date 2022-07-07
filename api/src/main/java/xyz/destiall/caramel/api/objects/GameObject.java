package xyz.destiall.caramel.api.objects;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.interfaces.StringWrapper;
import xyz.destiall.caramel.api.render.MeshRenderer;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.render.Renderer;
import xyz.destiall.caramel.api.interfaces.Render;
import xyz.destiall.caramel.api.interfaces.Update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This represents a group of {@link Component}s in one object.
 * This can also contain children {@link GameObject}s.
 */
public abstract class GameObject implements Update, Render {
    protected final Set<Component> components;
    public final LinkedList<GameObject> children;
    public final ArrayList<String> tags;
    public StringWrapper name;
    public Transform transform;
    public Transform parent;
    public Scene scene;
    public boolean active = true;
    public int id;

    public GameObject() {
        components = ConcurrentHashMap.newKeySet(3);
        children = new LinkedList<>();
        tags = new ArrayList<>();
    }

    public GameObject(Scene parentScene) {
        this.scene = parentScene;
        components = ConcurrentHashMap.newKeySet(3);
        children = new LinkedList<>();
        tags = new ArrayList<>();
        transform = new Transform(this);
        id = scene.generateId();
    }

    @Override
    public void update() {
        if (!active) return;
        for (GameObject child : children) {
            child.transform.position.set(transform.position);
            child.transform.rotation.set(transform.rotation);
            child.transform.scale.set(transform.scale);
            child.update();
        }

        for (Component component : components) {
            if (!component.enabled) continue;
            if (!component.alreadyEnabled) {
                component.start();
                component.alreadyEnabled = true;
            }
            component.update();
        }
    }

    @Override
    public void lateUpdate() {
        for (Component component : components) {
            if (!component.enabled || component instanceof MeshRenderer) continue;
            component.lateUpdate();
        }

        Set<Renderer> renderers = getComponentsInChildren(Renderer.class);
        for (Renderer render : renderers) {
            render.lateUpdate();
        }
    }

    @Override
    public void editorUpdate() {
        if (!active) return;
        for (GameObject child : children) {
            child.transform.position.set(transform.position);
            child.transform.rotation.set(transform.rotation);
            child.transform.scale.set(transform.scale);
            child.editorUpdate();
        }

        Set<Renderer> renderers = getComponentsInChildren(Renderer.class);
        for (Renderer render : renderers) {
            render.lateUpdate();
        }
    }

    /**
     * Set the {@link Scene} of this object.
     * It will remove itself from its previous {@link Scene}.
     * @param parentScene The {@link Scene} to set it to, not null.
     */
    public void setScene(Scene parentScene) {
        if (parentScene != null) {
            destroy(this);
            scene = parentScene;
            scene.addGameObject(this);
        }
    }

    /**
     * Get a {@link Component} that is linked to this object.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The {@link Component} if it exists, null if none.
     */
    public <C extends Component> C getComponent(Class<C> clazz) {
        Component component = components.stream().filter(c -> clazz.isAssignableFrom(c.getClass())).findFirst().orElse(null);
        return clazz.cast(component);
    }

    /**
     * Get a set of {@link Component}s that is linked to this object.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The set of {@link Component}. It cannot be null.
     */
    public <C extends Component> Set<C> getComponents(Class<C> clazz) {
        return (Set<C>) components.stream().filter(c -> clazz.isAssignableFrom(clazz)).collect(Collectors.toSet());
    }

    /**
     * Get a {@link Component} that is linked to this object's parent.
     * If this object does not have a parent, it will return null.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The {@link Component} if it exists, null if none.
     */
    public <C extends Component> C getComponentInParent(Class<C> clazz) {
        if (parent == null) return null;
        Component component = parent.gameObject.getComponent(clazz);
        if (component != null) return clazz.cast(component);
        return parent.gameObject.getComponentInParent(clazz);
    }

    /**
     * Get a {@link Component} that is linked to this object's children.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The {@link Component} if it exists, null if none.
     */
    public <C extends Component> C getComponentInChildren(Class<C> clazz) {
        Component component = getComponent(clazz);
        if (component != null) return clazz.cast(component);
        for (GameObject child : children) {
            component = child.getComponentInChildren(clazz);
            if (component != null) return clazz.cast(component);
        }
        return null;
    }

    /**
     * Get a set of {@link Component}s that is linked to this object's children.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The set of {@link Component}. It cannot be null.
     */
    public <C extends Component> Set<C> getComponentsInChildren(Class<C> clazz) {
        Set<C> set = new HashSet<>();
        C component = getComponent(clazz);
        if (component != null) set.add(component);
        getComponentsInChildren(clazz, set);
        return set;
    }

    private <C extends Component> void getComponentsInChildren(Class<C> clazz, Set<C> set) {
        for (GameObject child : children) {
            if (child.hasComponent(clazz)) {
                set.add(child.getComponent(clazz));
            }
            child.getComponentsInChildren(clazz, set);
        }
    }

    /**
     * Checks if this object has a given tag.
     * @param tag The tag to check
     * @return true if it exists, else false.
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public boolean addComponent(Component component) {
        if (hasComponent(component.getClass())) return false;
        components.add(component);
        return true;
    }

    /**
     * Remove a {@link Component} linked to this object.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return true if it successfully removed, else false.
     */
    public <C extends Component> boolean removeComponent(Class<C> clazz) {
        Component component = getComponent(clazz);
        if (component == null) return false;
        if (component instanceof Transform) return false;
        components.remove(component);
        if (scene != null) {
            if (component instanceof Camera && component == scene.getGameCamera()) {
                scene.setGameCamera(null);
            }
        }
        return true;
    }

    /**
     * Get every {@link Component} linked to this object.
     * This collection is mutable.
     * @return A mutable copy.
     */
    public Collection<Component> getMutableComponents() {
        return components;
    }

    /**
     * Get every {@link Component} linked to this object.
     * This collection is immutable.
     * @return An immutable copy.
     */
    public Collection<Component> getComponents() {
        List<Component> immutable = new ArrayList<>(components);
        immutable.sort(Comparator.comparingInt(a -> a.id));
        return immutable;
    }

    /**
     * Checks if this object contains a given {@link Component} class.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return true if it exists, else false.
     */
    public <C extends Component> boolean hasComponent(Class<C> clazz) {
        return components.stream().anyMatch(c -> clazz.isAssignableFrom(c.getClass()));
    }

    /**
     * Find a {@link GameObject} that matches this name.
     * @param name The name of the {@link GameObject} to find.
     * @return The matching {@link GameObject}, null if none found.
     */
    public GameObject findGameObject(String name) {
        if (scene != null) {
            return scene.findGameObject(name);
        }
        return null;
    }

    /**
     * Destroy a {@link GameObject} from its scene.
     * @param gameObject The {@link GameObject} to destroy.
     */
    public void destroy(GameObject gameObject) {
        if (scene != null) {
            scene.destroy(gameObject);
        }
    }

    @Override
    public void render(Camera camera) {
        if (!active) return;
        for (Component component : components) {
            if (component instanceof Render)
                ((Render) component).render(camera);
        }
        for (GameObject ch : children) {
            ch.render(camera);
        }
    }

    /**
     * Duplicate this object.
     * @param copyId Whether to copy its ID or generate a new unique ID.
     * @return The duplicated {@link GameObject}.
     */
    public abstract GameObject clone(boolean copyId);

    /**
     * Instantiate a new {@link GameObject} in this scene from its {@link Prefab}.
     * @param prefab The {@link Prefab} to instantiate from.
     * @param parent The parent {@link Transform} to add it to.
     * @return The newly instantiated {@link GameObject}.
     */
    public GameObject instantiate(GameObject prefab, Transform parent) {
        GameObject clone = prefab.clone(false);
        if (parent != null) {
            scene.addGameObject(clone, parent.gameObject);
        }
        return clone;
    }

    /**
     * Instantiate a new {@link GameObject} in this scene from its {@link Prefab}.
     * @param prefab The {@link Prefab} to instantiate from.
     * @return The newly instantiated {@link GameObject}.
     */
    public GameObject instantiate(GameObject prefab) {
        return instantiate(prefab, null);
    }
}
