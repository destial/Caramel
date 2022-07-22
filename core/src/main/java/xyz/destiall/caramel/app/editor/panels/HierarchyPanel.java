package xyz.destiall.caramel.app.editor.panels;

import caramel.api.components.RigidBody2D;
import caramel.api.physics.components.Box2DCollider;
import caramel.api.physics.components.Circle2DCollider;
import caramel.api.render.Text;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import org.joml.Vector4f;
import caramel.api.Application;
import caramel.api.Input;
import caramel.api.objects.GameObject;
import caramel.api.objects.GameObjectImpl;
import caramel.api.render.MeshRenderer;
import caramel.api.components.Transform;
import caramel.api.texture.Mesh;
import caramel.api.texture.MeshBuilder;
import caramel.api.objects.SceneImpl;
import xyz.destiall.caramel.app.editor.action.AddGameObjects;
import xyz.destiall.caramel.app.editor.action.DeleteGameObjects;
import xyz.destiall.caramel.app.utils.Payload;
import caramel.api.objects.StringWrapperImpl;

import java.util.concurrent.atomic.AtomicInteger;

public final class HierarchyPanel extends Panel {
    private boolean addingGameObjectHierarchy;
    private boolean editingGameObject;
    private ImVec2 popupMousePos;
    private GameObject editingGo;
    private GameObject hoveredGameObject;

    public HierarchyPanel(SceneImpl scene) {
        super(scene);
    }

    @Override
    public void __imguiLayer() {
        ImGui.begin("Hierarchy", ImGuiWindowFlags.NoCollapse);
        Panel.setPanelFocused(getClass(), ImGui.isWindowFocused());
        Panel.setPanelHovered(getClass(), ImGui.isWindowHovered());
        AtomicInteger index = new AtomicInteger(0);
        hoveredGameObject = null;
        for (GameObject gameObject : scene.getGameObjects()) {
            if (treeNode(gameObject, index)) ImGui.treePop();
        }

        if (ImGui.isWindowFocused() && ImGui.isWindowHovered()) {
            if (hoveredGameObject == null && !editingGameObject && ImGui.isMouseClicked(Input.Mouse.RIGHT)) {
                addingGameObjectHierarchy = !addingGameObjectHierarchy;
                editingGameObject = false;
                if (addingGameObjectHierarchy) {
                    popupMousePos = new ImVec2(Application.getApp().getMouseListener().getX(), Application.getApp().getMouseListener().getY());
                }
            } else if (ImGui.isMouseClicked(Input.Mouse.LEFT)) {
                addingGameObjectHierarchy = false;
                editingGameObject = false;
                if (hoveredGameObject == null) {
                    scene.getSelectedGameObject().clear();
                }
            }
        }

        if (ImGui.isWindowFocused()) {
            if (!scene.getSelectedGameObject().isEmpty()) {
                if (Input.isKeyPressed(Input.Key.BACKSPACE) || Input.isKeyPressed(Input.Key.DELETE)) {
                    DeleteGameObjects action = new DeleteGameObjects(scene);
                    for (GameObject go : scene.getSelectedGameObject()) {
                        scene.destroy(go);
                        action.deleted.add(go);
                    }
                    scene.addUndoAction(action);
                    scene.getSelectedGameObject().clear();
                }

                if (Input.isControlPressedAnd(Input.Key.C) && !scene.getSelectedGameObject().isEmpty()) {
                    Payload.COPIED_GAMEOBJECTS.addAll(scene.getSelectedGameObject());
                }

                if (Input.isControlPressedAnd(Input.Key.V) && !Payload.COPIED_GAMEOBJECTS.isEmpty()) {
                    AddGameObjects action = new AddGameObjects(scene);
                    for (GameObject copied : Payload.COPIED_GAMEOBJECTS) {
                        GameObject go = copied.clone(false);
                        action.added.add(go);
                        scene.addGameObject(go);
                    }
                    scene.addUndoAction(action);
                }

                if (Input.isControlPressedAnd(Input.Key.D) && !scene.getSelectedGameObject().isEmpty()) {
                    AddGameObjects action = new AddGameObjects(scene);
                    for (GameObject copied : scene.getSelectedGameObject()) {
                        GameObject go = copied.clone(false);
                        scene.addGameObject(go);
                        action.added.add(go);
                    }
                    scene.addUndoAction(action);
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
                Payload.COPIED_GAMEOBJECTS.clear();
                Payload.COPIED_GAMEOBJECTS.add(editingGo);
                editingGameObject = false;

            } else if (ImGui.selectable("Delete")) {
                DeleteGameObjects deleteGameObjects = new DeleteGameObjects(scene);
                scene.destroy(editingGo);
                deleteGameObjects.deleted.add(editingGo);
                scene.addUndoAction(deleteGameObjects);
                editingGameObject = false;

            }

            ImGui.end();
        } else if (addingGameObjectHierarchy) {
            ImGui.begin("##addgameobject", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoSavedSettings);
            ImGui.setWindowPos(popupMousePos.x, popupMousePos.y);

            if (ImGui.menuItem("Select All", "CTRL + A", false, !scene.getSelectedGameObject().isEmpty())) {
                scene.getSelectedGameObject().addAll(scene.getGameObjects());
                addingGameObjectHierarchy = false;
            }

            if (ImGui.menuItem("Copy", "CTRL + C", false, !scene.getSelectedGameObject().isEmpty())) {
                Payload.COPIED_GAMEOBJECTS.clear();
                Payload.COPIED_GAMEOBJECTS.addAll(scene.getSelectedGameObject());
                addingGameObjectHierarchy = false;
            }

            if (ImGui.menuItem("Paste", "CTRL + V", false, !Payload.COPIED_GAMEOBJECTS.isEmpty())) {
                AddGameObjects action = new AddGameObjects(scene);
                for (GameObject copied : Payload.COPIED_GAMEOBJECTS) {
                    GameObject go = copied.clone(false);
                    action.added.add(go);
                    scene.addGameObject(go);
                }
                scene.addUndoAction(action);
                addingGameObjectHierarchy = false;
            }

            ImGui.separator();

            if (ImGui.selectable("New Empty GameObject")) {
                GameObject go = new GameObjectImpl(scene);
                AddGameObjects addGameObjects = new AddGameObjects(scene);
                scene.addGameObject(go);
                addGameObjects.added.add(go);
                scene.addUndoAction(addGameObjects);
                addingGameObjectHierarchy = false;
            }

            if (ImGui.selectable("New 2D Cube")) {
                GameObject go = new GameObjectImpl(scene);
                MeshRenderer renderer = new MeshRenderer(go);
                RigidBody2D rigidBody = new RigidBody2D(go);
                Box2DCollider boxCollider = new Box2DCollider(go);

                go.addComponent(renderer);
                go.addComponent(rigidBody);
                go.addComponent(boxCollider);

                AddGameObjects addGameObjects = new AddGameObjects(scene);
                scene.addGameObject(go);
                addGameObjects.added.add(go);
                scene.addUndoAction(addGameObjects);
                addingGameObjectHierarchy = false;
            }

            if (ImGui.selectable("New 2D Circle")) {
                GameObject go = new GameObjectImpl(scene);
                MeshRenderer renderer = new MeshRenderer(go);
                Mesh mesh = MeshBuilder.createCircle(0.5f, 36);
                renderer.setMesh(mesh);
                mesh.build();
                RigidBody2D rigidBody = new RigidBody2D(go);
                Circle2DCollider CircleCollider = new Circle2DCollider(go);

                go.addComponent(renderer);
                go.addComponent(rigidBody);
                go.addComponent(CircleCollider);

                AddGameObjects addGameObjects = new AddGameObjects(scene);
                scene.addGameObject(go);
                addGameObjects.added.add(go);
                scene.addUndoAction(addGameObjects);
                addingGameObjectHierarchy = false;
            }

            if (ImGui.selectable("New UI Text")) {
                GameObject go = new GameObjectImpl(scene);
                Text renderer = new Text(go);
                go.transform.scale.x = 0.025f;
                go.transform.scale.y = 0.025f;
                go.addComponent(renderer);
                AddGameObjects addGameObjects = new AddGameObjects(scene);
                scene.addGameObject(go);
                addGameObjects.added.add(go);
                scene.addUndoAction(addGameObjects);
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
            hoveredGameObject = gameObject;
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
