package xyz.destiall.caramel.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector4f;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.components.MeshRenderer;
import xyz.destiall.caramel.editor.Scene;
import xyz.destiall.caramel.graphics.Mesh;
import xyz.destiall.caramel.graphics.MeshBuilder;
import xyz.destiall.caramel.objects.GameObject;

import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.glfw.GLFW.*;

public class HierarchyPanel extends Panel {
    private boolean addingComponentsHierarchy;
    private ImVec2 popupMousePos;

    public HierarchyPanel(Scene scene) {
        super(scene);
    }

    @Override
    public void imguiLayer() {
        ImGui.begin("Hierarchy");
        AtomicInteger index = new AtomicInteger(0);
        scene.hoveredGameObject = null;
        for (GameObject gameObject : scene.getGameObjects()) {
            if (treeNode(gameObject, index)) ImGui.treePop();
        }
        if (ImGui.isWindowHovered()) {
            if (ImGui.isMouseClicked(GLFW_MOUSE_BUTTON_2)) {
                addingComponentsHierarchy = !addingComponentsHierarchy;
                popupMousePos = new ImVec2(Application.getApp().getMouseListener().getX(), Application.getApp().getMouseListener().getY());
            } else if (ImGui.isMouseClicked(GLFW_MOUSE_BUTTON_1)) {
                addingComponentsHierarchy = false;
            } else if ((ImGui.isKeyDown(GLFW_KEY_BACKSPACE) || ImGui.isKeyDown(GLFW_KEY_DELETE)) && scene.selectedGameObject != null) {
                scene.destroy(scene.selectedGameObject);
                scene.selectedGameObject = null;
            }
        }
        if (addingComponentsHierarchy) {
            ImGui.begin("Add GameObject", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoSavedSettings);
            ImGui.setWindowPos(popupMousePos.x, popupMousePos.y);
            if (ImGui.selectable("Add GameObject")) {
                GameObject go = new GameObject(scene);
                Mesh quad = MeshBuilder.createQuad(1);
                quad.setColor(new Vector4f(1f, 1f, 1f, 1f));
                quad.build();
                go.addComponent(new MeshRenderer(go));
                go.getComponent(MeshRenderer.class).setMesh(quad);
                scene.addGameObject(go);
                addingComponentsHierarchy = false;
            }
            ImGui.end();
        }
        ImGui.end();
    }

    private boolean treeNode(GameObject gameObject, AtomicInteger index) {
        ImGui.pushID(index.getAndIncrement());
        if (scene.selectedGameObject == gameObject) {
            ImGui.pushStyleColor(ImGuiCol.Text, 0.5f, 0.5f, 0.5f, 1.0f);
        }
        boolean treeNode = ImGui.treeNodeEx("" + index, ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.FramePadding, gameObject.name);
        if (scene.selectedGameObject == gameObject) {
            ImGui.popStyleColor();
        }
        if (ImGui.isItemClicked()) {
            scene.selectedGameObject = gameObject;
        }
        if (ImGui.isItemHovered()) {
            scene.hoveredGameObject = gameObject;
        }
        ImGui.popID();

        if (ImGui.beginDragDropSource()) {
            ImGui.setDragDropPayload(Scene.SCENE_DRAG_DROP_PAYLOAD, gameObject);
            ImGui.text(gameObject.name);
            ImGui.endDragDropSource();
        }

        if (ImGui.beginDragDropTarget()) {
            GameObject payload = ImGui.acceptDragDropPayload(Scene.SCENE_DRAG_DROP_PAYLOAD, GameObject.class);
            if (payload != null && payload != gameObject) {
                scene.addGameObject(gameObject, payload);
            }
            ImGui.endDragDropTarget();
        }

        if (treeNode) {
            for (GameObject c : gameObject.children) {
                treeNode(c, index);
            }
        }

        return treeNode;
    }
}
