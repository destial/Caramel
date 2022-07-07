package xyz.destiall.caramel.api;

import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.physics.info.ContactPoint2D;
import xyz.destiall.caramel.api.interfaces.HideInEditor;
import xyz.destiall.caramel.api.interfaces.Update;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class Component implements Update {
    @HideInEditor private final String clazz = getClass().getName();
    @HideInEditor public transient Transform transform;
    @HideInEditor public transient GameObject gameObject;
    @HideInEditor public transient boolean alreadyEnabled = false;
    @HideInEditor public int id;

    @HideInEditor public boolean enabled = true;

    protected Component() {}

    public Component(GameObject gameObject) {
        this.gameObject = gameObject;
        this.transform = gameObject.transform;
        id = gameObject.scene.generateId();
    }

    public abstract void start();
    public void lateUpdate() {}

    public void onCollisionEnter(RigidBody2D other) {}

    public void onCollisionExit(RigidBody2D other) {}

    public void onCollisionEnterRaw(ContactPoint2D point2D) {}

    public void onCollisionExitRaw(ContactPoint2D point2D) {}

    public <C extends Component> C getComponent(Class<C> clazz) {
        return gameObject.getComponent(clazz);
    }

    public <C extends Component> C getComponentInParent(Class<C> clazz) {
        return gameObject.getComponentInParent(clazz);
    }

    public <C extends Component> C getComponentInChildren(Class<C> clazz) {
        return gameObject.getComponentInChildren(clazz);
    }

    public <C extends Component> boolean removeComponent(Class<C> clazz) {
        return gameObject.removeComponent(clazz);
    }

    public <C extends Component> boolean hasComponent(Class<C> clazz) {
        return gameObject.hasComponent(clazz);
    }

    public void destroy(GameObject gameObject) {
        gameObject.destroy(gameObject);
    }

    public Component clone(GameObject gameObject, boolean copyId) {
        try {
            Component clone = getClass().getDeclaredConstructor(GameObject.class).newInstance(gameObject);
            gameObject.scene.entityIds.decrementAndGet();
            for (Field field : getClass().getFields()) {
                try {
                    if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
                    boolean prev = field.isAccessible();
                    field.setAccessible(true);
                    field.set(clone, field.get(this));
                    field.setAccessible(prev);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (copyId) {
                clone.id = id;
            } else {
                clone.id = gameObject.scene.generateId();
            }
            return clone;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
