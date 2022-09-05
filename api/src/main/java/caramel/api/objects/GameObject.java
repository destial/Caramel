package caramel.api.objects;

import caramel.api.Application;
import caramel.api.Component;
import caramel.api.components.Camera;
import caramel.api.components.Transform;
import caramel.api.interfaces.Render;
import caramel.api.interfaces.StringWrapper;
import caramel.api.interfaces.Update;
import caramel.api.render.MeshRenderer;
import caramel.api.render.Renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This represents a group of {@link Component}s in one object.
 * This can also contain children {@link GameObject}s.
 */
public abstract class GameObject implements Update, Render {
    protected final Set<Component> components;
    public final LinkedList<GameObject> children;
    public final ArrayList<String> tags;
    public transient Transform transform;

    public StringWrapper name;
    public Transform parent;
    public Scene scene;
    public boolean active = true;
    public int id;

    public GameObject() {
        components = ConcurrentHashMap.newKeySet(3);
        children = new LinkedList<>();
        tags = new ArrayList<>();
    }

    public GameObject(final Scene parentScene) {
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
        for (final GameObject child : children) {
            child.transform.position.set(transform.position);
            child.transform.rotation.set(transform.rotation);
            child.update();
        }

        for (final Component component : components) {
            if (!component.enabled) continue;
            if (!component.alreadyEnabled) {
                try {
                    component.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                component.alreadyEnabled = true;
            }
            component.update();
        }
    }

    @Override
    public void lateUpdate() {
        for (final Component component : components) {
            if (!component.enabled || component instanceof MeshRenderer) continue;
            component.lateUpdate();
        }

        final Set<Renderer> renderers = getComponentsInChildren(Renderer.class);
        for (final Renderer render : renderers) {
            render.lateUpdate();
        }
    }

    @Override
    public void editorUpdate() {
        if (!active) return;
        for (final GameObject child : children) {
            child.transform.position.set(transform.position);
            child.transform.rotation.set(transform.rotation);
            child.editorUpdate();
        }

        final Set<Renderer> renderers = getComponentsInChildren(Renderer.class);
        for (final Renderer render : renderers) {
            render.lateUpdate();
        }
    }

    /**
     * Set the {@link Scene} of this object.
     * It will remove itself from its previous {@link Scene}.
     * @param parentScene The {@link Scene} to set it to, not null.
     */
    public void setScene(final Scene parentScene) {
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
    public <C extends Component> C getComponent(final Class<C> clazz) {
        final Component component = components.stream().filter(c -> clazz.isAssignableFrom(c.getClass())).findFirst().orElse(null);
        return clazz.cast(component);
    }

    /**
     * Get a {@link Component} that is linked to this object.
     * @param clazz The class of the {@link Component}.
     * @return The {@link Component} if it exists, null if none.
     */
    public Component getComponent(final String clazz) {
        return components.stream().filter(c -> c.getClass().getSimpleName().equals(clazz)).findFirst().orElse(null);
    }

    /**
     * Get a set of {@link Component}s that is linked to this object.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The set of {@link Component}. It cannot be null.
     */
    public <C extends Component> Set<C> getComponents(final Class<C> clazz) {
        return (Set<C>) components.stream().filter(c -> clazz.isAssignableFrom(c.getClass())).collect(Collectors.toSet());
    }

    /**
     * Get a {@link Component} that is linked to this object's parent.
     * If this object does not have a parent, it will return null.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The {@link Component} if it exists, null if none.
     */
    public <C extends Component> C getComponentInParent(final Class<C> clazz) {
        if (parent == null) return null;
        final Component component = parent.gameObject.getComponent(clazz);
        if (component != null) return clazz.cast(component);
        return parent.gameObject.getComponentInParent(clazz);
    }

    /**
     * Get a {@link Component} that is linked to this object's children.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The {@link Component} if it exists, null if none.
     */
    public <C extends Component> C getComponentInChildren(final Class<C> clazz) {
        Component component = getComponent(clazz);
        if (component != null) return clazz.cast(component);
        for (final GameObject child : children) {
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
    public <C extends Component> Set<C> getComponentsInChildren(final Class<C> clazz) {
        final Set<C> set = new HashSet<>();
        final C component = getComponent(clazz);
        if (component != null) set.add(component);
        getComponentsInChildren(clazz, set);
        return set;
    }

    private <C extends Component> void getComponentsInChildren(final Class<C> clazz, final Set<C> set) {
        for (final GameObject child : children) {
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
    public boolean hasTag(final String tag) {
        return tags.contains(tag);
    }

    /**
     * Add a {@link Component} to this {@link GameObject}.
     * @param component The {@link Component} to add.
     * @return true if a component of the same type doesn't exist, else false.
     */
    public boolean addComponent(final Component component) {
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
    public <C extends Component> boolean removeComponent(final Class<C> clazz) {
        final Component component = getComponent(clazz);
        if (component == null) return false;
        if (component instanceof Transform) return false;
        components.remove(component);
        if (scene != null) {
            if (component instanceof Camera) {
                scene.removeGameCamera((Camera) component);
            }
        }
        return true;
    }

    /**
     * Remove a {@link Component} linked to this object.
     * @param clazz The class of the {@link Component}.
     * @return true if it successfully removed, else false.
     */
    public boolean removeComponent(final String clazz) {
        final Component component = getComponent(clazz);
        if (component == null) return false;
        if (component instanceof Transform) return false;
        components.remove(component);
        if (scene != null) {
            if (component instanceof Camera) {
                scene.removeGameCamera((Camera) component);
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
    public <C extends Component> boolean hasComponent(final Class<C> clazz) {
        return components.stream().anyMatch(c -> clazz.isAssignableFrom(c.getClass()));
    }

    /**
     * Find a {@link GameObject} that matches this name.
     * @param name The name of the {@link GameObject} to find.
     * @return The matching {@link GameObject}, null if none found.
     */
    public GameObject findGameObject(final String name) {
        if (scene != null) {
            return scene.findGameObject(name);
        }
        return null;
    }

    /**
     * Destroy a {@link GameObject} from its scene.
     * @param gameObject The {@link GameObject} to destroy.
     */
    public void destroy(final GameObject gameObject) {
        if (scene != null) {
            scene.destroy(gameObject);
        }
    }

    /**
     * Destroy a {@link GameObject} from its scene.
     * @param gameObject The {@link GameObject} to destroy.
     * @param timeout The time to wait in milliseconds.
     */
    public void destroy(final GameObject gameObject, final long timeout) {
        Application.getApp().getScheduler().runTaskLater(() -> destroy(gameObject), timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void render(final Camera camera) {
        if (!active) return;
        for (final Component component : components) {
            if (component instanceof Render) {
                final Render render = ((Render) component);
                if (render instanceof Renderer) {
                    if (((Renderer) render).getRenderState() != camera.getState()) continue;
                }
                render.render(camera);
            }
        }
        for (final GameObject ch : children) {
            ch.render(camera);
        }
    }

    /**
     * Duplicate this object.
     * @param copyId Whether to copy its ID or generate a new unique ID.
     * @return The duplicated {@link GameObject}.
     */
    public abstract GameObject clone(final boolean copyId);

    /**
     * Instantiate a new {@link GameObject} in this scene from its {@link Prefab}.
     * @param prefab The {@link Prefab} to instantiate from.
     * @param parent The parent {@link Transform} to add it to.
     * @return The newly instantiated {@link GameObject}.
     */
    public GameObject instantiate(final GameObject prefab, final Transform parent) {
        final GameObject clone = prefab.clone(false);
        if (parent != null) {
            scene.addGameObject(clone, parent.gameObject);
        }
        return clone;
    }

    @Override
    public String toString() {
        return "GameObject{" +
                "tags=" + tags +
                ", name=" + name +
                ", id=" + id +
                '}';
    }

    /**
     * Instantiate a new {@link GameObject} in this scene from its {@link Prefab}.
     * @param prefab The {@link Prefab} to instantiate from.
     * @return The newly instantiated {@link GameObject}.
     */
    public GameObject instantiate(final GameObject prefab) {
        return instantiate(prefab, null);
    }
}
