package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import org.joml.Vector4f;
import xyz.destiall.caramel.api.Application;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.objects.GameObjectImpl;
import xyz.destiall.caramel.api.render.MeshRenderer;
import xyz.destiall.caramel.api.components.Transform;
import xyz.destiall.caramel.api.texture.Mesh;
import xyz.destiall.caramel.api.texture.MeshBuilder;
import xyz.destiall.caramel.api.objects.SceneImpl;
import xyz.destiall.caramel.app.utils.Payload;
import xyz.destiall.caramel.api.objects.StringWrapperImpl;

import java.util.concurrent.atomic.AtomicInteger;

public final class HierarchyPanel extends Panel {
    private boolean addingGameObjectHierarchy;
    private boolean editingGameObject;
    private ImVec2 popupMousePos;
    private GameObject editingGo;

    public HierarchyPanel(SceneImpl scene) {
        super(scene);
    }

    @Override
    public void __imguiLayer() {
        ImGui.begin("Hierarchy", ImGuiWindowFlags.NoCollapse);
        Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
        Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
        AtomicInteger index = new AtomicInteger(0);
        scene.hoveredGameObject = null;
        for (GameObject gameObject : scene.getGameObjects()) {
            if (treeNode(gameObject, index)) ImGui.treePop();
        }

        if (ImGui.isWindowFocused() && ImGui.isWindowHovered()) {
            if (scene.hoveredGameObject == null && !editingGameObject && ImGui.isMouseClicked(Input.Mouse.RIGHT)) {
                addingGameObjectHierarchy = !addingGameObjectHierarchy;
                editingGameObject = false;
                if (addingGameObjectHierarchy) {
                    popupMousePos = new ImVec2(Application.getApp().getMouseListener().getX(), Application.getApp().getMouseListener().getY());
                }
            } else if (ImGui.isMouseClicked(Input.Mouse.LEFT)) {
                addingGameObjectHierarchy = false;
                editingGameObject = false;
                if (scene.hoveredGameObject == null) {
                    System.out.println("test");
                    scene.getSelectedGameObject().clear();
                }
            }
        }

        if (ImGui.isWindowFocused() && ImGui.isWindowHovered()) {
            if (!scene.getSelectedGameObject().isEmpty()) {
                if (Input.isKeyDown(Input.Key.BACKSPACE) || Input.isKeyDown(Input.Key.DELETE)) {
                    for (GameObject go : scene.getSelectedGameObject()) {
                        scene.destroy(go);
                    }
                    scene.getSelectedGameObject().clear();
                }

                if (Input.isControlPressedAnd(Input.Key.C) && !scene.getSelectedGameObject().isEmpty()) {
                    Payload.COPIED.addAll(scene.getSelectedGameObject());
                }

                if (Input.isControlPressedAnd(Input.Key.V) && !Payload.COPIED.isEmpty()) {
                    for (GameObject copied : Payload.COPIED) {
                        GameObject go = copied.clone(false);
                        scene.addGameObject(go);
                    }
                    addingGameObjectHierarchy = false;
                }

                if (Input.isControlPressedAnd(Input.Key.D) && !scene.getSelectedGameObject().isEmpty()) {
                    for (GameObject copied : scene.getSelectedGameObject()) {
                        GameObject go = copied.clone(false);
                        scene.addGameObject(go);
                    }
                    addingGameObjectHierarchy = false;
                }
            }

            if (Input.isControlPressedAnd(Input.Key.A)) {
                scene.getSelectedGameObject().addAll(scene.getGameObjects());
            }
        }

        if (editingGameObject) {
            ImGui.begin("##editgameobject", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoSavedSettings);
            ImGui.setWindowPos(popupMousePos.x, popupMousePos.y);
            if (ImGui.selectable("Copy")) {
                Payload.COPIED.clear();
                Payload.COPIED.add(editingGo);
                editingGameObject = false;
            } else if (ImGui.selectable("Delete")) {
                scene.destroy(editingGo);
                editingGameObject = false;
            }
            ImGui.end();
        } else if (addingGameObjectHierarchy) {
            ImGui.begin("##addgameobject", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoSavedSettings);
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
                addingGameObjectHierarchy = false;
            }

            if (ImGui.menuItem("Select All", "CTRL + A", false, !scene.getSelectedGameObject().isEmpty())) {
                scene.getSelectedGameObject().addAll(scene.getGameObjects());
                addingGameObjectHierarchy = false;
            }

            if (ImGui.menuItem("Copy", "CTRL + C", false, !scene.getSelectedGameObject().isEmpty())) {
                Payload.COPIED.clear();
                Payload.COPIED.addAll(scene.getSelectedGameObject());
                addingGameObjectHierarchy = false;
            }

            if (ImGui.menuItem("Paste", "CTRL + V", false, !Payload.COPIED.isEmpty())) {
                for (GameObject copied : Payload.COPIED) {
                    GameObject go = copied.clone(false);
                    scene.addGameObject(go);
                }
                addingGameObjectHierarchy = false;
            }
            ImGui.end();
        }

        ImGui.end();
    }

    private boolean treeNode(GameObject gameObject, AtomicInteger index) {
        ImString gameObjectName = ((StringWrapperImpl) gameObject.name).imString();

        if (!gameObject.active) {
            ImGui.pushStyleColor(ImGuiCol.Text, 0.5f, 0.5f, 0.5f, 1.0f);
        }

        if (scene.getSelectedGameObject().contains(gameObject)) {
            ImGui.pushStyleColor(ImGuiCol.Text, 0.1f, 0.3f, 0.5f, 1.0f);
        }

        boolean treeNode = ImGui.treeNodeEx(index.incrementAndGet(), ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.FramePadding, gameObjectName.get());
        if (scene.getSelectedGameObject().contains(gameObject)) {
            ImGui.popStyleColor();
        }

        if (!gameObject.active) {
            ImGui.popStyleColor();
        }

        if (ImGui.isItemClicked()) {
            if (!Input.isKeyDown(Input.Key.L_CONTROL)) {
                scene.getSelectedGameObject().clear();
            }
            scene.getSelectedGameObject().add(gameObject);
        }

        if (ImGui.isItemHovered()) {
            scene.hoveredGameObject = gameObject;
        }

        if (ImGui.isMouseClicked(Input.Mouse.RIGHT) && ImGui.isItemHovered()) {
            addingGameObjectHierarchy = false;
            editingGameObject = true;
            editingGo = gameObject;
            popupMousePos = new ImVec2(Application.getApp().getMouseListener().getX(), Application.getApp().getMouseListener().getY());
            if (!Input.isKeyDown(Input.Key.L_CONTROL)) {
                scene.getSelectedGameObject().clear();
            }
            scene.getSelectedGameObject().add(gameObject);
        }

        if (ImGui.beginDragDropSource()) {
            ImGui.setDragDropPayload(Payload.DRAG_DROP_GAMEOBJECT_HIERARCHY, gameObject);
            ImGui.text(gameObject.name.get());
            ImGui.endDragDropSource();
        }

        if (ImGui.beginDragDropTarget()) {
            GameObject payload = ImGui.acceptDragDropPayload(Payload.DRAG_DROP_GAMEOBJECT_HIERARCHY, GameObject.class);
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
