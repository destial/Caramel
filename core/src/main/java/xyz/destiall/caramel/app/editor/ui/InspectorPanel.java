package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.ComponentImpl;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.render.SpriteRenderer;
import xyz.destiall.caramel.api.objects.GameObjectImpl;
import xyz.destiall.caramel.api.render.MeshRenderer;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.components.RigidBody3D;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.physics.components.Box2DCollider;
import xyz.destiall.caramel.api.physics.components.Box3DCollider;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.api.utils.FileIO;
import xyz.destiall.caramel.app.editor.SceneImpl;
import xyz.destiall.caramel.app.utils.StringWrapperImpl;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;

public class InspectorPanel extends Panel {
    public static final Set<Class<?>> COMPONENTS = new HashSet<>();
    static {
        COMPONENTS.add(Camera.class);
        COMPONENTS.add(MeshRenderer.class);
        COMPONENTS.add(SpriteRenderer.class);
        COMPONENTS.add(RigidBody2D.class);
        COMPONENTS.add(RigidBody3D.class);
        COMPONENTS.add(Box2DCollider.class);
        COMPONENTS.add(Box3DCollider.class);
    }

    private final ImString search = new ImString();
    private boolean addingComponents;
    private boolean addingScript;
    private boolean removingComponents;
    private ImVec2 popupMousePos;

    public InspectorPanel(SceneImpl scene) {
        super(scene);
    }

    @Override
    public void imguiLayer() {
        ImGui.begin("Inspector");
        Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
        Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
        if (scene.selectedGameObject != null) {
            scene.selectedGameObject.active = ImGuiUtils.drawCheckBox("active", scene.selectedGameObject.active);
            ImGuiUtils.inputText("name", ((StringWrapperImpl) scene.selectedGameObject.name).imString());
            Component removing = null;
            for (Component component : scene.selectedGameObject.getComponents()) {

                if (ImGui.collapsingHeader(component.getClass().getSimpleName())) {
                    component.__imguiLayer();
                    if (component instanceof Transform) continue;
                    if (ImGui.isWindowHovered()) {
                        if (ImGui.isMouseClicked(GLFW_MOUSE_BUTTON_2)) {
                            removingComponents = !removingComponents;
                            popupMousePos = new ImVec2(ApplicationImpl.getApp().getMouseListener().getX(), ApplicationImpl.getApp().getMouseListener().getY());
                        } else if (ImGui.isMouseClicked(GLFW_MOUSE_BUTTON_1)) {
                            removingComponents = false;
                        }
                    }
                    if (removingComponents) {
                        ImGui.begin("Remove Component", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoSavedSettings);
                        ImGui.setWindowPos(popupMousePos.x, popupMousePos.y);
                        if (ImGui.selectable("Remove Component")) {
                            removing = component;
                            removingComponents = false;
                        }
                        ImGui.end();
                    }
                }
                
            }
            if (removing != null) {
                scene.selectedGameObject.removeComponent(removing.getClass());
            }
            ImGui.separator();
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
                    if (scene.selectedGameObject.hasComponent((Class<? extends ComponentImpl>) c)) continue;
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
                    String scriptName = ImGuiUtils.inputText("##newScript", "NewScript");
                    if (ImGui.button("Create")) {
                        FileIO.writeScript(scriptName);
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
                gameObject.addComponent((ComponentImpl) instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}