package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import xyz.destiall.caramel.api.Application;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.interfaces.FunctionButton;
import xyz.destiall.caramel.api.interfaces.HideInEditor;
import xyz.destiall.caramel.api.interfaces.ShowInEditor;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.components.RigidBody3D;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.physics.components.Box2DCollider;
import xyz.destiall.caramel.api.physics.components.Box3DCollider;
import xyz.destiall.caramel.api.scripts.InternalScript;
import xyz.destiall.caramel.api.scripts.Script;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.api.utils.FileIO;
import xyz.destiall.caramel.api.objects.SceneImpl;
import xyz.destiall.caramel.app.ui.ImGuiUtils;
import xyz.destiall.caramel.app.utils.Payload;
import xyz.destiall.caramel.api.objects.StringWrapperImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;

public final class InspectorPanel extends Panel {
    private static final TextEditor editor = new TextEditor();
    static {
        TextEditorLanguageDefinition lang = new TextEditorLanguageDefinition();
        lang.setAutoIdentation(false);
        lang.setName("java");
        lang.setSingleLineComment("//");
        lang.setCommentStart("/*");
        lang.setCommentEnd("*/");
        lang.setKeywords(new String[] {"for", "while", "do", "if", "else", "break", "continue", "return", "class", "interface", "extends", "implements", "public", "private", "protected", "final", "import", "package", "new", "this"});
        editor.setLanguageDefinition(lang);
    }
    private final ImString search = new ImString();
    private final ImString scriptName = new ImString();
    private boolean addingComponents;
    private boolean addingScript;
    private Component selectedComponent;
    private ImVec2 popupMousePos;

    public InspectorPanel(SceneImpl scene) {
        super(scene);
    }

    @Override
    public void __imguiLayer() {
        ImGui.begin("Inspector");
        Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
        Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());

        GameObject selected = scene.getSelectedGameObject().stream().findFirst().orElse(null);
        if (selected != null) {
            selected.active = ImGuiUtils.drawCheckBox("active", selected.active);
            ImGuiUtils.inputText("name", ((StringWrapperImpl) selected.name).imString());
            for (Component component : selected.getComponents()) {
                if (selectedComponent == component) {
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.3f, 0.3f, 0.7f, 1.0f);
                }
                if (ImGui.collapsingHeader(component.getClass().getSimpleName())) {
                    ImGui.text("id: " + component.id);
                    for (Field field : component.getClass().getFields()) {
                        if (field.isAnnotationPresent(HideInEditor.class) || Modifier.isTransient(field.getModifiers()))
                            continue;
                        if (Modifier.isPublic(field.getModifiers()) || field.isAnnotationPresent(ShowInEditor.class)) {
                            field.setAccessible(true);
                            ImGuiUtils.imguiLayer(field, component);
                        }
                    }
                    ImGui.separator();
                    for (Method method : component.getClass().getMethods()) {
                        if (method.isAnnotationPresent(FunctionButton.class)) {
                            method.setAccessible(true);
                            ImGuiUtils.imguiLayer(method, component);
                        }
                    }

                    if (component instanceof Script) {
                        InternalScript s = Application.getApp().getScriptManager().getInternalScript(component.getClass());
                        if (s != null) {
                            editor.setText((String) s.getCode());
                            editor.render((String) s.getCode());
                        }
                    }
                }
                if (selectedComponent == component) {
                    ImGui.popStyleColor();
                }
                if (component instanceof Transform) continue;

                if (ImGui.isItemHovered() && ImGui.isMouseClicked(Input.Mouse.RIGHT)) {
                    selectedComponent = component;
                    popupMousePos = new ImVec2(ApplicationImpl.getApp().getMouseListener().getX(), ApplicationImpl.getApp().getMouseListener().getY());
                }

                if (ImGui.isWindowHovered() && ImGui.isMouseClicked(Input.Mouse.LEFT)) {
                    selectedComponent = null;
                }
            }

            if (selectedComponent != null) {
                ImGui.begin("##removecomponent", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoSavedSettings);
                ImGui.setWindowPos(popupMousePos.x, popupMousePos.y);
                if (ImGui.selectable("Remove Component")) {
                    selected.removeComponent(selectedComponent.getClass());
                    selectedComponent = null;
                }
                ImGui.end();
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
                for (Class<?> c : Payload.COMPONENTS) {
                    if (selected.hasComponent((Class<? extends Component>) c)) continue;
                    if (search.isEmpty() || c.getSimpleName().toLowerCase().contains(search.get().toLowerCase())) {
                        if (ImGui.selectable(c.getSimpleName())) {
                            addComponent(selected, c);
                            addingComponents = false;
                        }
                    }
                }
                ImGui.separator();
                if (ImGui.selectable("Create Script")) {
                    addingScript = !addingScript;
                }
                if (addingScript) {
                    ImGuiUtils.inputText("Script Name:", this.scriptName);
                    if (ImGui.button("Create")) {
                        InternalScript s = FileIO.writeScript(this.scriptName.get());
                        if (s == null) {
                            Debug.logError("Error while creating script file!");
                        } else {
                            try {
                                Component c = s.getAsComponent(selected);
                                selected.addComponent(c);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Debug.logError(e.getLocalizedMessage());
                            }
                        }
                        addingComponents = false;
                        addingScript = false;
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
                if (componentClass.isAssignableFrom(Box2DCollider.class) && !gameObject.hasComponent(RigidBody2D.class)) {
                    RigidBody2D rigidBody = new RigidBody2D(gameObject);
                    gameObject.addComponent(rigidBody);
                }
                if (componentClass.isAssignableFrom(Box3DCollider.class) && !gameObject.hasComponent(RigidBody3D.class)) {
                    RigidBody3D rigidBody = new RigidBody3D(gameObject);
                    gameObject.addComponent(rigidBody);
                }
                Object instance = componentClass.getConstructor(GameObject.class).newInstance(gameObject);
                gameObject.addComponent((Component) instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
