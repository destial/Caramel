package xyz.destiall.caramel.api;

import imgui.ImGui;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.interfaces.FunctionButton;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.physics.info.ContactPoint2D;
import xyz.destiall.caramel.app.editor.ui.ImGuiUtils;
import xyz.destiall.caramel.api.interfaces.HideInEditor;
import xyz.destiall.caramel.api.interfaces.ShowInEditor;
import xyz.destiall.caramel.api.interfaces.Update;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    @Override
    public void imguiLayer() {
        ImGui.text("id: " + id);
        for (Field field : getClass().getFields()) {
            if (field.isAnnotationPresent(HideInEditor.class) || Modifier.isTransient(field.getModifiers())) continue;
            if (Modifier.isPublic(field.getModifiers()) || field.isAnnotationPresent(ShowInEditor.class)) {
                field.setAccessible(true);
                ImGuiUtils.imguiLayer(field, this);
            }
        }
        ImGui.separator();
        for (Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(FunctionButton.class)) {
                method.setAccessible(true);
                ImGuiUtils.imguiLayer(method, this);
            }
        }
    }

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
                gameObject.scene.entityIds.decrementAndGet();
                clone.id = id;
            }
            return clone;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
