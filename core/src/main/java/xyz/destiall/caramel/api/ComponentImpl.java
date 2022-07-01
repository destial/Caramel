package xyz.destiall.caramel.api;

import imgui.ImGui;
import xyz.destiall.caramel.api.interfaces.FunctionButton;
import xyz.destiall.caramel.api.interfaces.HideInEditor;
import xyz.destiall.caramel.api.interfaces.ShowInEditor;
import xyz.destiall.caramel.api.objects.GameObjectImpl;
import xyz.destiall.caramel.app.editor.ui.ImGuiUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class ComponentImpl extends Component {
    protected ComponentImpl() {}

    public ComponentImpl(GameObjectImpl gameObject) {
        this.gameObject = gameObject;
        this.transform = gameObject.transform;
        id = gameObject.scene.generateId();
    }

    public abstract void start();
    public void lateUpdate() {}

    @Override
    public void __imguiLayer() {
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
}
