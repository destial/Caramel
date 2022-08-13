package xyz.destiall.caramel.app.editor.panels;

import caramel.api.Application;
import caramel.api.Input;
import caramel.api.components.RigidBody2D;
import caramel.api.components.RigidBody3D;
import caramel.api.components.Transform;
import caramel.api.debug.Debug;
import caramel.api.objects.GameObject;
import caramel.api.objects.GameObjectImpl;
import caramel.api.objects.SceneImpl;
import caramel.api.objects.StringWrapperImpl;
import caramel.api.physics.components.Box2DCollider;
import caramel.api.physics.components.Box3DCollider;
import caramel.api.physics.components.Circle2DCollider;
import caramel.api.render.Button;
import caramel.api.render.MeshRenderer;
import caramel.api.render.Text;
import caramel.api.texture.mesh.CubeMesh;
import caramel.api.texture.mesh.Mesh;
import caramel.api.texture.mesh.CircleMesh;
import caramel.api.texture.mesh.IcosahedronMesh;
import caramel.api.texture.mesh.QuadMesh;
import caramel.api.texture.mesh.TriangleMesh;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import xyz.destiall.caramel.app.editor.action.AddGameObjects;
import xyz.destiall.caramel.app.editor.action.DeleteGameObjects;
import xyz.destiall.caramel.app.editor.action.EditorAction;
import xyz.destiall.caramel.app.utils.Payload;

import java.util.concurrent.atomic.AtomicInteger;

import static xyz.destiall.caramel.app.ui.ImGUILayer.TERTIARY_COLOR;

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
        int flags = ImGuiWindowFlags.NoCollapse;
        if (window.getScriptManager().isRecompiling()) {
            flags |= ImGuiWindowFlags.NoInputs | ImGuiWindowFlags.NoMouseInputs;
        }
        if (ImGui.begin("Hierarchy", flags)) {
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
                        popupMousePos = new ImVec2(ImGui.getMousePosX(), ImGui.getMousePosY());
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
                    if (ImGui.isKeyPressed(Input.Key.BACKSPACE) || ImGui.isKeyPressed(Input.Key.DELETE)) {
                        DeleteGameObjects action = new DeleteGameObjects(scene);
                        for (GameObject go : scene.getSelectedGameObject()) {
                            scene.destroy(go);
                            action.deleted.add(go);
                        }
                        scene.addUndoAction(action);
                        scene.getSelectedGameObject().clear();
                    }

                    if ((ImGui.getIO().getKeyCtrl()) && ImGui.isKeyPressed(Input.Key.C) && !scene.getSelectedGameObject().isEmpty()) {
                        Payload.COPIED_GAMEOBJECTS.addAll(scene.getSelectedGameObject());
                    }

                    if ((ImGui.getIO().getKeyCtrl()) && ImGui.isKeyPressed(Input.Key.V) && !Payload.COPIED_GAMEOBJECTS.isEmpty()) {
                        AddGameObjects action = new AddGameObjects(scene);
                        for (GameObject copied : Payload.COPIED_GAMEOBJECTS) {
                            GameObject go = copied.clone(false);
                            action.added.add(go);
                            scene.addGameObject(go);
                        }
                        scene.addUndoAction(action);
                    }

                    if ((ImGui.getIO().getKeyCtrl()) && ImGui.isKeyPressed(Input.Key.D) && !scene.getSelectedGameObject().isEmpty()) {
                        AddGameObjects action = new AddGameObjects(scene);
                        for (GameObject copied : scene.getSelectedGameObject()) {
                            GameObject go = copied.clone(false);
                            scene.addGameObject(go);
                            action.added.add(go);
                        }
                        scene.addUndoAction(action);
                    }
                }

                if ((ImGui.getIO().getKeyCtrl()) && ImGui.isKeyPressed(Input.Key.A)) {
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
                    scene.addGameObject(go);

                    AddGameObjects addGameObjects = new AddGameObjects(scene);
                    addGameObjects.added.add(go);
                    scene.addUndoAction(addGameObjects);
                    addingGameObjectHierarchy = false;
                }

                if (ImGui.selectable("New 2D Quad")) {
                    GameObject go = new GameObjectImpl(scene);
                    go.name.set("Quad");

                    go.addComponent(new MeshRenderer(go, new QuadMesh()));
                    go.addComponent(new RigidBody2D(go));
                    go.addComponent(new Box2DCollider(go));
                    scene.addGameObject(go);

                    AddGameObjects addGameObjects = new AddGameObjects(scene);
                    addGameObjects.added.add(go);
                    scene.addUndoAction(addGameObjects);
                    addingGameObjectHierarchy = false;
                }

                if (ImGui.selectable("New 2D Circle")) {
                    GameObject go = new GameObjectImpl(scene);
                    go.name.set("Circle");

                    go.addComponent(new MeshRenderer(go, new CircleMesh(0.5f, 36)));
                    go.addComponent(new RigidBody2D(go));
                    go.addComponent(new Circle2DCollider(go));
                    scene.addGameObject(go);

                    AddGameObjects addGameObjects = new AddGameObjects(scene);
                    addGameObjects.added.add(go);
                    scene.addUndoAction(addGameObjects);
                    addingGameObjectHierarchy = false;
                }

                if (ImGui.selectable("New 2D Triangle")) {
                    GameObject go = new GameObjectImpl(scene);
                    go.name.set("Triangle");

                    go.addComponent(new MeshRenderer(go, new TriangleMesh()));
                    go.addComponent(new RigidBody2D(go));
                    go.addComponent(new Circle2DCollider(go));
                    scene.addGameObject(go);

                    AddGameObjects addGameObjects = new AddGameObjects(scene);
                    addGameObjects.added.add(go);
                    scene.addUndoAction(addGameObjects);
                    addingGameObjectHierarchy = false;
                }

                if (ImGui.selectable("New 3D Cube")) {
                    GameObject go = new GameObjectImpl(scene);
                    go.name.set("Cube");

                    go.addComponent(new MeshRenderer(go, new CubeMesh()));
                    go.addComponent(new RigidBody3D(go));
                    go.addComponent(new Box3DCollider(go));
                    scene.addGameObject(go);

                    AddGameObjects addGameObjects = new AddGameObjects(scene);
                    addGameObjects.added.add(go);
                    scene.addUndoAction(addGameObjects);
                    addingGameObjectHierarchy = false;
                }

                if (ImGui.selectable("New 3D Sphere")) {
                    GameObject go = new GameObjectImpl(scene);
                    go.name.set("Sphere");

                    go.addComponent(new MeshRenderer(go, new IcosahedronMesh()));
                    go.addComponent(new RigidBody3D(go));
                    scene.addGameObject(go);

                    AddGameObjects addGameObjects = new AddGameObjects(scene);
                    addGameObjects.added.add(go);
                    scene.addUndoAction(addGameObjects);
                    addingGameObjectHierarchy = false;
                }

                if (ImGui.selectable("New UI Text")) {
                    GameObject go = new GameObjectImpl(scene);
                    go.name.set("Text");
                    go.transform.scale.x = 0.025f;
                    go.transform.scale.y = 0.025f;
                    go.addComponent(new Text(go));
                    scene.addGameObject(go);

                    AddGameObjects addGameObjects = new AddGameObjects(scene);
                    addGameObjects.added.add(go);
                    scene.addUndoAction(addGameObjects);
                    addingGameObjectHierarchy = false;
                }

                if (ImGui.selectable("New UI Button")) {
                    GameObject button = new GameObjectImpl(scene);
                    button.name.set("Button");
                    button.addComponent(new Button(button));
                    button.transform.scale.x = 2.7f;

                    GameObject text = new GameObjectImpl(scene);
                    text.name.set("Text");
                    text.transform.scale.x = 0.025f;
                    text.transform.scale.y = 0.025f;
                    text.addComponent(new Text(text));
                    button.children.add(text);
                    scene.addGameObject(button);

                    AddGameObjects addGameObjects = new AddGameObjects(scene);
                    addGameObjects.added.add(button);
                    addGameObjects.added.add(text);
                    scene.addUndoAction(addGameObjects);
                    addingGameObjectHierarchy = false;
                }
                ImGui.end();
            }
        }
        ImGui.end();
    }

    private boolean treeNode(GameObject gameObject, AtomicInteger index) {
        ImString gameObjectName = ((StringWrapperImpl) gameObject.name).imString();

        if (!gameObject.active) {
            ImGui.pushStyleColor(ImGuiCol.Text, 0.5f, 0.5f, 0.5f, 1.0f);
        }

        if (scene.getSelectedGameObject().contains(gameObject)) {
            ImGui.pushStyleColor(ImGuiCol.Text, TERTIARY_COLOR.x, TERTIARY_COLOR.y, TERTIARY_COLOR.z, 1.f);
        }

        boolean treeNode = ImGui.treeNodeEx(index.incrementAndGet(), ImGuiTreeNodeFlags.DefaultOpen | ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.FramePadding, gameObjectName.get());
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
            if (!ImGui.isKeyDown(Input.Key.L_CONTROL)) {
                scene.getSelectedGameObject().clear();
            }
            scene.getSelectedGameObject().add(gameObject);
        }

        if (ImGui.beginDragDropSource()) {
            if (ImGui.getDragDropPayload(Payload.DRAG_DROP_GAMEOBJECT_HIERARCHY) == null) {
                ImGui.setDragDropPayload(Payload.DRAG_DROP_GAMEOBJECT_HIERARCHY, gameObject);
            }
            ImGui.text(gameObject.name.get());
            ImGui.endDragDropSource();
        }

        if (ImGui.beginDragDropTarget() && !ImGui.isMouseDown(Input.Mouse.LEFT)) {
            GameObject payload = ImGui.getDragDropPayload(Payload.DRAG_DROP_GAMEOBJECT_HIERARCHY);
            if (payload != null) {
                ImGui.setDragDropPayload(Payload.DRAG_DROP_GAMEOBJECT_HIERARCHY, null);
                Transform parent = payload.parent;
                if (parent != null) {
                    parent.gameObject.children.remove(payload);
                }

                scene.addGameObject(payload, gameObject);
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
