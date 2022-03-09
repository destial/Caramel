package xyz.destiall.caramel.components;

import imgui.ImGui;
import imgui.type.ImString;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.destiall.caramel.editor.ui.ImGuiUtils;
import xyz.destiall.caramel.graphics.Mesh;
import xyz.destiall.caramel.graphics.Texture;
import xyz.destiall.caramel.interfaces.HideInEditor;
import xyz.destiall.caramel.interfaces.ShowInEditor;
import xyz.destiall.caramel.interfaces.Update;
import xyz.destiall.caramel.objects.GameObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class Component implements Update {
    @HideInEditor public transient Transform transform;
    @HideInEditor public transient GameObject gameObject;
    @HideInEditor public transient boolean alreadyEnabled = false;
    @HideInEditor private transient final ImString string = new ImString();

    public boolean enabled = true;

    public Component(GameObject gameObject) {
        this.gameObject = gameObject;
        this.transform = gameObject.transform;
    }

    public abstract void start();
    public void lateUpdate() {}

    @Override
    public void imguiLayer() {
        for (Field field : getClass().getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(HideInEditor.class) || Modifier.isTransient(field.getModifiers())) continue;
                if (Modifier.isPublic(field.getModifiers()) || field.isAnnotationPresent(ShowInEditor.class)) {
                    field.setAccessible(true);
                    Class<?> type = field.getType();
                    Object value = field.get(this);
                    String name = field.getName();

                    if (type == int.class) {
                        int i = (int) value;
                        int newI = ImGuiUtils.dragInt(name, i);
                        field.set(this, newI);
                    } else if (type == float.class) {
                        float f = (float) value;
                        float newF = ImGuiUtils.dragFloat(name, f);
                        field.set(this, newF);
                    } else if (type == Vector3f.class) {
                        ImGuiUtils.drawVec3Control(name, (Vector3f) value, 1f);
                    } else if (type == Vector2f.class) {
                        ImGuiUtils.drawVec2Control(name, (Vector2f) value);
                    } else if (type == Quaternionf.class) {
                        Quaternionf quat = (Quaternionf) value;
                        ImGuiUtils.drawQuatControl(name, quat, 0.f);
                    } else if (type == Mesh.class) {
                        Mesh mesh = (Mesh) value;
                        ImGui.text("shader: " + mesh.getShader().getPath());
                        if (mesh.getColor() != null && ImGuiUtils.colorPicker4("color", mesh.getColor())) {
                            mesh.setColor(mesh.getColor());
                        }
                        ImGui.text("texture: ");
                        ImGui.sameLine();
                        ImGui.inputText("##texture", string);
                        ImGui.sameLine();
                        if (ImGui.button("apply")) {
                            if (mesh.getTexture() == null || !mesh.getTexture().getPath().equalsIgnoreCase(string.get())) {
                                Texture texture = new Texture(string.get());
                                if (texture.isLoaded()) {
                                    mesh.setTexture(texture);
                                }
                            }
                            string.clear();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            field.setAccessible(false);
        }
    }

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
            Component clone = getClass().getConstructor(GameObject.class).newInstance(gameObject);
            for (Field field : getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (Modifier.isTransient(field.getModifiers())) continue;
                    field.set(clone, field.get(this));
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
}
