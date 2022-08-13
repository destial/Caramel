package caramel.api;

import caramel.api.components.RigidBody2D;
import caramel.api.components.Transform;
import caramel.api.interfaces.Copyable;
import caramel.api.interfaces.HideInEditor;
import caramel.api.interfaces.Update;
import caramel.api.objects.GameObject;
import caramel.api.physics.info.ContactPoint2D;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.destiall.java.reflection.Reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

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

    /**
     * Get a {@link Component} that is linked to this object.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The {@link Component} if it exists, null if none.
     */
    public <C extends Component> C getComponent(Class<C> clazz) {
        return gameObject.getComponent(clazz);
    }

    /**
     * Get a {@link Component} that is linked to this object.
     * @param clazz The class of the {@link Component}.
     * @return The {@link Component} if it exists, null if none.
     */
    public Component getComponent(String clazz) {
        return gameObject.getComponent(clazz);
    }

    /**
     * Get a {@link Component} that is linked to this object's parent.
     * If this object does not have a parent, it will return null.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The {@link Component} if it exists, null if none.
     */
    public <C extends Component> C getComponentInParent(Class<C> clazz) {
        return gameObject.getComponentInParent(clazz);
    }

    /**
     * Get a {@link Component} that is linked to this object's children.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return The {@link Component} if it exists, null if none.
     */
    public <C extends Component> C getComponentInChildren(Class<C> clazz) {
        return gameObject.getComponentInChildren(clazz);
    }

    /**
     * Remove a {@link Component} linked to this object.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return true if it successfully removed, else false.
     */
    public <C extends Component> boolean removeComponent(Class<C> clazz) {
        return gameObject.removeComponent(clazz);
    }

    /**
     * Checks if this object contains a given {@link Component} class.
     * @param clazz The class of the {@link Component}.
     * @param <C> {@link Component}
     * @return true if it exists, else false.
     */
    public <C extends Component> boolean hasComponent(Class<C> clazz) {
        return gameObject.hasComponent(clazz);
    }

    /**
     * Destroy a {@link GameObject} from its scene.
     * @param gameObject The {@link GameObject} to destroy.
     */
    public void destroy(GameObject gameObject) {
        gameObject.destroy(gameObject);
    }

    /**
     * Destroy a {@link GameObject} from its scene.
     * @param gameObject The {@link GameObject} to destroy.
     * @param timeout The time to wait in milliseconds.
     */
    public void destroy(GameObject gameObject, long timeout) {
        gameObject.destroy(gameObject, timeout);
    }

    /**
     * Invoke a method in this {@link Component}. This will completely bypass security access by private or protected modifiers.
     * @param methodName The method to invoke.
     */
    public void sendMessage(String methodName) {
        Method m = Reflect.getDeclaredMethod(this.getClass(), methodName);
        m.setAccessible(true);
        Reflect.invokeMethod(this, methodName);
    }

    /**
     * Invoke a method in this {@link Component}. This will completely bypass security access by private or protected modifiers.
     * @param methodName The method to invoke.
     * @param timeout The time to wait in milliseconds.
     */
    public void sendMessage(String methodName, long timeout) {
        Application.getApp().getScheduler().runTaskLater(() -> sendMessage(methodName), timeout, TimeUnit.MILLISECONDS);
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

                    Object value = field.get(this);
                    if (value != null) {
                        if (field.getType().isAssignableFrom(Copyable.class)) {
                            field.set(clone, ((Copyable<?>) value).copy());
                        } else if (field.getType().isAssignableFrom(Vector2f.class)) {
                            field.set(clone, new Vector2f((Vector2f) value));
                        } else if (field.getType().isAssignableFrom(Vector3f.class)) {
                            field.set(clone, new Vector3f((Vector3f) value));
                        } else if (field.getType().isAssignableFrom(Vector4f.class)) {
                            field.set(clone, new Vector4f((Vector4f) value));
                        } else if (field.getType().isAssignableFrom(Matrix4f.class)) {
                            field.set(clone, new Matrix4f((Matrix4f) value));
                        } else {
                            field.set(clone, value);
                        }
                    }
                    field.setAccessible(prev);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            clone.id = copyId ? id : gameObject.scene.generateId();
            return clone;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                ", gameObject=" + gameObject.name +
                ", id=" + id +
                ", enabled=" + enabled +
                '}';
    }
}
