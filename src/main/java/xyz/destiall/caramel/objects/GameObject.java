package xyz.destiall.caramel.objects;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.destiall.caramel.components.Component;
import xyz.destiall.caramel.components.MeshRenderer;
import xyz.destiall.caramel.components.Transform;
import xyz.destiall.caramel.editor.Scene;
import xyz.destiall.caramel.interfaces.Render;
import xyz.destiall.caramel.interfaces.Update;

import java.util.*;

public class GameObject implements Update, Render, Cloneable {
    private final Map<Class<? extends Component>, Component> components;

    public final List<GameObject> children;
    public Transform transform;
    public Transform parent;
    public String name;
    public Scene scene;

    public GameObject(Scene parentScene) {
        this.scene = parentScene;
        name = "GameObject";
        components = new HashMap<>();
        children = new LinkedList<>();
        transform = new Transform(this);
    }

    @Override
    public void update() {
        for (GameObject child : children) {
            Vector3f newPos = new Vector3f(transform.position);
            Quaternionf newRot = new Quaternionf(transform.rotation);
            Vector3f newScale = new Vector3f(transform.scale);
            child.transform.position.set(newPos);
            child.transform.rotation.set(newRot);
            child.transform.scale.set(newScale);
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
            if (!component.enabled) continue;
            component.lateUpdate();
        }
    }

    @Override
    public void editorUpdate() {
        for (GameObject child : children) {
            Vector3f newPos = new Vector3f(transform.position);
            Quaternionf newRot = new Quaternionf(transform.rotation);
            Vector3f newScale = new Vector3f(transform.scale);
            child.transform.position.set(newPos);
            child.transform.rotation.set(newRot);
            child.transform.scale.set(newScale);
            child.editorUpdate();
        }

        MeshRenderer render = getComponent(MeshRenderer.class);
        if (render != null) {
            render.lateUpdate();
        }
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
        Component component;
        for (GameObject child : children) {
            component = child.getComponent(clazz);
            if (component == null) component = child.getComponentInChildren(clazz);
            if (component != null) return clazz.cast(component);
        }
        return null;
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

    public GameObject findGameObject(String name) {
        return scene.findGameObject(name);
    }

    public void destroy(GameObject gameObject) {
        scene.destroy(gameObject);
    }

    public <C extends Component> boolean hasComponent(Class<C> clazz) {
        return components.containsKey(clazz);
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
        clone.name = name;
        clone.transform.position.set(new Vector3f(transform.position));
        clone.transform.localPosition.set(new Vector3f(transform.localPosition));
        clone.transform.rotation.set(new Quaternionf(transform.rotation));
        clone.transform.localRotation.set(new Quaternionf(transform.localRotation));
        clone.transform.scale.set(new Vector3f(transform.scale));
        clone.transform.localScale.set(new Vector3f(transform.localScale));
        clone.transform.forward.set(new Vector3f(transform.forward));
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
}
