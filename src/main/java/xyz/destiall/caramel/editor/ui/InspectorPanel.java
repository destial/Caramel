package xyz.destiall.caramel.editor.ui;

import imgui.ImGui;
import imgui.type.ImString;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.MeshRenderer;
import xyz.destiall.caramel.app.utils.FileIO;
import xyz.destiall.caramel.editor.Scene;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;

public class InspectorPanel extends Panel {
    public static final Set<Class<?>> COMPONENTS = new HashSet<>();
    static {
        COMPONENTS.add(Camera.class);
        COMPONENTS.add(MeshRenderer.class);
    }

    private final ImString search = new ImString();
    private boolean addingComponents;
    private boolean addingScript;

    public InspectorPanel(Scene scene) {
        super(scene);
    }

    @Override
    public void imguiLayer() {
        ImGui.begin("Inspector");
        if (scene.selectedGameObject != null) {
            for (Component component : scene.selectedGameObject.getComponents()) {
                if (ImGui.collapsingHeader(component.getClass().getSimpleName()))
                    component.imguiLayer();
            }
            if (ImGui.button("Add Component")) {
                addingComponents = !addingComponents;
            }
            if (addingComponents) {
                ImGui.beginListBox("##List Component");
                ImGui.inputText("Search", search);
                if (ImGui.isKeyPressed(GLFW_KEY_BACKSPACE)) {
                    String newInput = search.getLength() > 0 ? search.get().substring(0, search.getLength() - 1) : "";
                    search.clear();
                    search.set(newInput);
                }
                for (Class<?> c : COMPONENTS) {
                    if (scene.selectedGameObject.hasComponent((Class<? extends Component>) c)) continue;
                    if (search.isEmpty() || c.getSimpleName().toLowerCase().contains(search.get().toLowerCase())) {
                        if (ImGui.selectable(c.getSimpleName())) {
                            addComponent(scene.selectedGameObject, c);
                            addingComponents = false;
                        }
                    }
                }
                if (ImGui.selectable("Script")) {
                    addingScript = !addingScript;
                }
                if (addingScript) {
                    ImString scriptName = new ImString("NewScript");
                    ImGui.inputText("##name", scriptName);
                    if (ImGui.button("Create")) {
                        FileIO.writeScript(scriptName.get());
                        addingComponents = false;
                    }
                }
                ImGui.endListBox();
            } else {
                search.clear();
            }

        }
        ImGui.end();
    }


    private void addComponent(GameObject gameObject, Class<?> componentClass) {
        if (Component.class.isAssignableFrom(componentClass)) {
            try {
                Object instance = componentClass.getConstructor(GameObject.class).newInstance(gameObject);
                gameObject.addComponent((Component) instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
