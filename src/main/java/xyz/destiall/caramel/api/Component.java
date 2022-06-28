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
    public static final AtomicInteger ENTITY_IDS = new AtomicInteger(0);

    @HideInEditor private final String clazz = getClass().getName();
    @HideInEditor public transient Transform transform;
    @HideInEditor public transient GameObject gameObject;
    @HideInEditor public transient boolean alreadyEnabled = false;
    @HideInEditor public int id;

    @HideInEditor public boolean enabled = true;

    private Component() {}

    public Component(GameObject gameObject) {
        this.gameObject = gameObject;
        this.transform = gameObject.transform;
        id = ENTITY_IDS.incrementAndGet();
        while (!gameObject.scene.entityIdExists(id)) {
            id = ENTITY_IDS.incrementAndGet();
        }
    }

    public abstract void start();
    public void lateUpdate() {}

    @Override
    public void imguiLayer() {
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

    public Component clone(GameObject gameObject) {
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
            return clone;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static class IDGetter extends AtomicInteger {
        private final AtomicInteger id;

        public IDGetter(int initialValue) {
            super(initialValue);
            id = new AtomicInteger(initialValue);
        }

        public IDGetter() {
            this(0);
        }

        @Override
        public String toString() {
            return super.toString();
        }

        @Override
        public int intValue() {
            return super.intValue();
        }

        @Override
        public long longValue() {
            return super.longValue();
        }

        @Override
        public float floatValue() {
            return super.floatValue();
        }

        @Override
        public double doubleValue() {
            return super.doubleValue();
        }

        @Override
        public byte byteValue() {
            return super.byteValue();
        }

        @Override
        public short shortValue() {
            return super.shortValue();
        }
    }
}
