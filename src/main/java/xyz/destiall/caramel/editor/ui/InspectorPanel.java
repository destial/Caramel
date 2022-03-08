package xyz.destiall.caramel.editor.ui;

import imgui.ImGui;
import imgui.type.ImString;
import xyz.destiall.caramel.components.Camera;
import xyz.destiall.caramel.components.Component;
import xyz.destiall.caramel.components.MeshRenderer;
import xyz.destiall.caramel.editor.CreateScript;
import xyz.destiall.caramel.editor.Scene;
import xyz.destiall.caramel.objects.GameObject;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;

public class InspectorPanel extends Panel {
    private final ImString search = new ImString();
    private static final Class<?>[] components = { Camera.class,  MeshRenderer.class };
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
                for (Class<?> c : components) {
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
                    if (ImGui.inputText("##name", scriptName)) {
                        CreateScript.create(scriptName.get());
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
