package xyz.destiall.caramel.api;

import xyz.destiall.caramel.api.components.MeshRenderer;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.app.editor.Scene;
import xyz.destiall.caramel.interfaces.Render;
import xyz.destiall.caramel.interfaces.Update;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameObject implements Update, Render, Cloneable {
    private final Map<Class<? extends Component>, Component> components;
    public final List<GameObject> children;
    public Transform transform;
    public Transform parent;
    public String name;
    public Scene scene;
    public int id;

    private GameObject() {
        components = new HashMap<>();
        children = new LinkedList<>();
    }

    public GameObject(Scene parentScene) {
        this.scene = parentScene;
        name = "GameObject";
        components = new HashMap<>();
        children = new LinkedList<>();
        transform = new Transform(this);
        id = Component.ENTITY_IDS.incrementAndGet();
    }

    @Override
    public void update() {
        for (GameObject child : children) {
            child.transform.position.set(transform.position);
            child.transform.rotation.set(transform.rotation);
            child.transform.scale.set(transform.scale);
            child.update();
        }

        for (Component component : components.values()) {
            if (!component.enabled) continue;
            if (!component.alreadyEnabled) {
                component.start();
                component.alreadyEnabled = true;
            }
            component.update();
        }
        for (Component component : components.values()) {
            if (!component.enabled || component instanceof MeshRenderer) continue;
            component.lateUpdate();
        }

        MeshRenderer render = getComponent(MeshRenderer.class);
        if (render != null) render.lateUpdate();
    }

    @Override
    public void editorUpdate() {
        for (GameObject child : children) {
            child.transform.position.set(transform.position);
            child.transform.rotation.set(transform.rotation);
            child.transform.scale.set(transform.scale);
            child.editorUpdate();
        }

        MeshRenderer render = getComponent(MeshRenderer.class);
        if (render != null) render.lateUpdate();
    }

    public void setScene(Scene parentScene) {
        if (parentScene != null) {
            destroy(this);
            this.scene = parentScene;
            scene.addGameObject(this);
        }
    }

    public <C extends Component> C getComponent(Class<C> clazz) {
        return clazz.cast(components.get(clazz));
    }

    public <C extends Component> C getComponentInParent(Class<C> clazz) {
        if (parent == null) return null;
        Component component = parent.gameObject.getComponent(clazz);
        if (component != null) return clazz.cast(component);
        return parent.gameObject.getComponentInParent(clazz);
    }

    public <C extends Component> C getComponentInChildren(Class<C> clazz) {
        Component component = getComponent(clazz);
        if (component != null) return clazz.cast(component);
        for (GameObject child : children) {
            component = child.getComponentInChildren(clazz);
            if (component != null) return clazz.cast(component);
        }
        return null;
    }

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

    public boolean addComponent(Component component) {
        if (components.containsKey(component.getClass())) return false;
        components.put(component.getClass(), component);
        return true;
    }

    public <C extends Component> boolean removeComponent(Class<C> clazz) {
        return components.remove(clazz) != null;
    }

    public Collection<Component> getComponents() {
        return components.values();
    }

    public <C extends Component> boolean hasComponent(Class<C> clazz) {
        return components.containsKey(clazz);
    }

    public GameObject findGameObject(String name) {
        return scene.findGameObject(name);
    }

    public void destroy(GameObject gameObject) {
        scene.destroy(gameObject);
    }

    @Override
    public void render() {
        for (Component component : components.values()) {
            if (component instanceof Render)
                ((Render) component).render();
        }
        for (GameObject ch : children) {
            ch.render();
        }
    }

    @Override
    public GameObject clone() {
        GameObject clone = new GameObject(scene);
        Component.ENTITY_IDS.decrementAndGet();
        clone.id = id;
        clone.name = name;
        clone.transform.position.set(transform.position);
        clone.transform.localPosition.set(transform.localPosition);
        clone.transform.rotation.set(transform.rotation);
        clone.transform.localRotation.set(transform.localRotation);
        clone.transform.scale.set(transform.scale);
        clone.transform.localScale.set(transform.localScale);
        clone.transform.forward.set(transform.forward);
        clone.transform.enabled = transform.enabled;
        for (Component c : components.values()) {
            if (c instanceof Transform) continue;
            Component cl = c.clone(clone);
            clone.addComponent(cl);
        }
        for (GameObject c : children) {
            GameObject ch = c.clone();
            ch.parent = clone.transform;
            clone.children.add(ch);
        }
        return clone;
    }

    public GameObject instantiate(GameObject prefab, Transform parent) {
        GameObject clone = prefab.clone();
        if (parent != null) {
            scene.addGameObject(clone, parent.gameObject);
        }
        return clone;
    }

    public GameObject instantiate(GameObject prefab) {
        return instantiate(prefab, null);
    }
}
