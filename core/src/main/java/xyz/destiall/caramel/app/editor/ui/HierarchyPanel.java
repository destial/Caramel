package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import org.joml.Vector4f;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.objects.GameObjectImpl;
import xyz.destiall.caramel.api.render.MeshRenderer;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.texture.Mesh;
import xyz.destiall.caramel.api.texture.MeshBuilder;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.editor.SceneImpl;
import xyz.destiall.caramel.app.utils.StringWrapperImpl;

import java.util.concurrent.atomic.AtomicInteger;

public final class HierarchyPanel extends Panel {
    private boolean addingComponentsHierarchy;
    private boolean editingGameObject;
    private int nodeId;
    private ImVec2 popupMousePos;
    private GameObject editingGo;

    public HierarchyPanel(SceneImpl scene) {
        super(scene);
    }

    @Override
    public void imguiLayer() {
        ImGui.begin("Hierarchy");
        Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
        Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
        AtomicInteger index = new AtomicInteger(0);
        scene.hoveredGameObject = null;
        for (GameObject gameObject : scene.getGameObjects()) {
            if (treeNode(gameObject, index)) ImGui.treePop();
        }

        if (ImGui.isWindowHovered()) {
            if (scene.hoveredGameObject == null && !editingGameObject && ImGui.isMouseClicked(Input.Mouse.RIGHT)) {
                addingComponentsHierarchy = !addingComponentsHierarchy;
                editingGameObject = false;
                if (addingComponentsHierarchy)
                    popupMousePos = new ImVec2(ApplicationImpl.getApp().getMouseListener().getX(), ApplicationImpl.getApp().getMouseListener().getY());
            } else if (ImGui.isMouseClicked(Input.Mouse.LEFT)) {
                addingComponentsHierarchy = false;
                editingGameObject = false;
            }
        }

        if (ImGui.isWindowFocused() && scene.selectedGameObject != null) {
            if (Input.isKeyDown(Input.Key.BACKSPACE) || Input.isKeyDown(Input.Key.DELETE)) {
                scene.destroy(scene.selectedGameObject);
                scene.selectedGameObject = null;
            } else if ((Input.isKeyDown(Input.Key.L_CONTROL) || Input.isKeyDown(Input.Key.R_CONTROL)) && Input.isKeyPressed(Input.Key.D)) {
                GameObject dupe = scene.selectedGameObject.clone(false);
                dupe.id = scene.generateId();
                scene.addGameObject(dupe);
                scene.selectedGameObject = dupe;
            }
        }

        if (editingGameObject) {
            ImGui.begin("Edit GameObject", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoSavedSettings);
            ImGui.setWindowPos(popupMousePos.x, popupMousePos.y);
            if (ImGui.selectable("Rename")) {
                
                editingGameObject = false;
            } else if (ImGui.selectable("Delete")) {
                scene.destroy(editingGo);
                editingGameObject = false;
            }
            ImGui.end();
        } else if (addingComponentsHierarchy) {
            ImGui.begin("Add GameObject", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoSavedSettings);
            ImGui.setWindowPos(popupMousePos.x, popupMousePos.y);
            if (ImGui.selectable("Add GameObject")) {
                GameObject go = new GameObjectImpl(scene);
                Mesh quad = MeshBuilder.createQuad(1);
                quad.setColor(new Vector4f(1f, 1f, 1f, 1f));
                quad.build();
                MeshRenderer renderer = new MeshRenderer(go);
                renderer.setMesh(quad);
                go.addComponent(renderer);
                scene.addGameObject(go);
                addingComponentsHierarchy = false;
            }
            ImGui.end();
        }
        ImGui.end();
    }

    private boolean treeNode(GameObject gameObject, AtomicInteger index) {
        ImString gameObjectName = ((StringWrapperImpl) gameObject.name).imString();

        if (scene.selectedGameObject == gameObject) {
            ImGui.pushStyleColor(ImGuiCol.Text, 0.5f, 0.5f, 0.5f, 1.0f);
        }

        boolean treeNode = ImGui.treeNodeEx(index.incrementAndGet(), ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.FramePadding, gameObjectName.get());
        if (scene.selectedGameObject == gameObject) {
            ImGui.popStyleColor();
        }

        if (ImGui.isItemClicked()) {
            scene.selectedGameObject = gameObject;
        }

        if (ImGui.isItemHovered()) {
            scene.hoveredGameObject = gameObject;
        }

        if (ImGui.isMouseClicked(Input.Mouse.RIGHT) && ImGui.isItemHovered()) {
            addingComponentsHierarchy = false;
            editingGameObject = true;
            editingGo = gameObject;
            popupMousePos = new ImVec2(ApplicationImpl.getApp().getMouseListener().getX(), ApplicationImpl.getApp().getMouseListener().getY());
            scene.selectedGameObject = gameObject;
            nodeId = index.get();
        }

        if (ImGui.beginDragDropSource()) {
            ImGui.setDragDropPayload(SceneImpl.SCENE_DRAG_DROP_PAYLOAD, gameObject);
            ImGui.text(gameObject.name.get());
            ImGui.endDragDropSource();
        }

        if (ImGui.beginDragDropTarget()) {
            GameObject payload = ImGui.acceptDragDropPayload(SceneImpl.SCENE_DRAG_DROP_PAYLOAD, GameObject.class);
            if (payload != null && payload != gameObject) {
                Transform parent = payload.parent;
                boolean canAdd = true;
                while (parent != null && canAdd) {
                    if (parent == gameObject.transform) {
                        canAdd = false;
                    }
                    parent = parent.transform;

                }
                if (canAdd) scene.addGameObject(gameObject, payload);
            }
            ImGui.endDragDropTarget();
        }

        if (treeNode) {
            for (GameObject c : gameObject.children) {
                if (treeNode(c, index)) ImGui.treePop();
            }
        }

        return treeNode;
    }
}
