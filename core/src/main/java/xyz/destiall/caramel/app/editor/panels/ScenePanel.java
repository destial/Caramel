package xyz.destiall.caramel.app.editor.panels;

import caramel.api.Input;
import caramel.api.Time;
import caramel.api.components.EditorCamera;
import caramel.api.debug.DebugImpl;
import caramel.api.objects.GameObject;
import caramel.api.objects.SceneImpl;
import caramel.api.render.BatchRenderer;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Matrix3d;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.editor.action.AddGameObjects;
import xyz.destiall.caramel.app.editor.action.DeleteGameObjects;
import xyz.destiall.caramel.app.editor.action.EditTransformComponent;
import xyz.destiall.caramel.app.ui.ImGuiUtils;
import xyz.destiall.caramel.app.utils.Payload;

import java.util.HashSet;
import java.util.Set;

public final class ScenePanel extends Panel {
    private final ApplicationImpl window;
    private final ImVec2 startMouseCoords = new ImVec2(0, 0);
    private final Vector3f selectionColor = new Vector3f(1.f, 0.f, 0.f);
    private int operation = Operation.TRANSLATE;
    private EditTransformComponent editTransformComponent;
    private boolean dragging = false;
    private float previousDt;
    private float leftX, rightX, topY, bottomY;

    public ScenePanel(SceneImpl scene) {
        super(scene);
        window = ApplicationImpl.getApp();
    }

    @Override
    public void __imguiLayer() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f);
        ImGui.begin("Scene", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse | ImGuiWindowFlags.AlwaysAutoResize);
        Panel.setPanelFocused(ScenePanel.class, ImGui.isWindowFocused());
        Panel.setPanelHovered(ScenePanel.class, ImGui.isWindowHovered());

        if (Time.isSecond) {
            previousDt = Time.deltaTime;
        }
        ImGui.text("Delta: " + previousDt + "ms");
        ImGui.text("Draw Calls: " + BatchRenderer.DRAW_CALLS);

        ImVec2 windowSize = getLargestAspectRatioViewport();
        ImVec2 windowPos = getCenteredPositionForViewport(windowSize);

        ImGui.setCursorPos(windowPos.x, windowPos.y);

        ImVec2 bottomLeft = new ImVec2();
        ImGui.getCursorScreenPos(bottomLeft);
        bottomLeft.x -= ImGui.getScrollX();
        bottomLeft.y -= ImGui.getScrollY();

        leftX = bottomLeft.x;
        bottomY = bottomLeft.y;
        rightX = leftX + windowSize.x;
        topY = bottomY + windowSize.y;

        int texId = window.getSceneViewFramebuffer().getTexture().getTexId();

        ImGui.image(texId, windowSize.x, windowSize.y, 0, 1, 1, 0);
        if (ImGui.button("T")) {
            operation = Operation.TRANSLATE;
        }
        ImGui.sameLine();
        if (ImGui.button("S")) {
            operation = Operation.SCALE;
        }
        ImGui.sameLine();
        if (ImGui.button("R")) {
            operation = Operation.ROTATE;
        }
        BatchRenderer.USE_BATCH = ImGuiUtils.drawCheckBox("batch render", BatchRenderer.USE_BATCH);

        window.getMouseListener().setSceneViewport(new Vector2f(leftX, bottomY));
        window.getMouseListener().setSceneViewportSize(new Vector2f(windowSize.x, windowSize.y));

        if (ImGui.isMouseDown(Input.Mouse.LEFT) && ImGui.isWindowHovered() && !dragging) {
            GameObject clicked = getClicked();
            if (clicked == null) {
                dragging = true;
                startMouseCoords.set(window.getMouseListener().getOrthoX(), window.getMouseListener().getOrthoY());
            }
        }

        if (ImGui.isMouseClicked(Input.Mouse.LEFT) && ImGui.isWindowFocused() && ImGui.isWindowHovered()) {
            GameObject clicked = getClicked();
            if (clicked != null) {
                if (!ImGui.getIO().getKeyCtrl()) {
                    window.getCurrentScene().getSelectedGameObject().clear();
                }
                window.getCurrentScene().getSelectedGameObject().add(clicked);
                dragging = false;
            }
        }

        if (ImGui.isMouseReleased(Input.Mouse.LEFT) && dragging) {
            ImVec2 endMouseCoords = new ImVec2(window.getMouseListener().getOrthoX(), window.getMouseListener().getOrthoY());
            Set<GameObject> objects = getSelected(startMouseCoords, endMouseCoords);
            if (objects != null) {
                if (!ImGui.getIO().getKeyCtrl()) {
                    window.getCurrentScene().getSelectedGameObject().clear();
                }
                window.getCurrentScene().getSelectedGameObject().addAll(objects);
            }
            dragging = false;
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

        GameObject selected = window.getCurrentScene().getSelectedGameObject().stream().findFirst().orElse(null);

        if (selected != null && !scene.isPlaying()) {
            ImGuizmo.setEnabled(true);
            ImGuizmo.setOrthographic(true);
            ImGuizmo.setDrawList();

            ImGuizmo.setRect(leftX, bottomY, windowSize.x, windowSize.y);

            EditorCamera camera = scene.getEditorCamera();
            Matrix4f inverseView = camera.getView();
            Matrix4f projection = camera.getProjection();
            Matrix4f transform = selected.transform.getModel();

            float[] view = new float[16];
            inverseView.get(view);
            float[] proj = new float[16];
            projection.get(proj);
            float[] model = new float[16];
            transform.get(model);

            float[] t = new float[3];
            float[] r = new float[3];
            float[] s = new float[3];

            ImGuizmo.manipulate(view, proj, model, operation, Mode.LOCAL);

            if (ImGuizmo.isUsing()) {
                ImGuizmo.decomposeMatrixToComponents(model, t, r, s);
                for (int i = 0; i < 3; i++) {
                    if (Float.isNaN(t[i])) {
                        t[i] = (i == 2 ? -1f : 0f) + 0.0000001f;
                    }
                }
                for (int i = 0; i < 3; i++) {
                    if (Float.isNaN(r[i])) {
                        r[i] = 0f + 0.0000001f;
                    }
                }
                for (int i = 0; i < 3; i++) {
                    if (Float.isNaN(s[i])) {
                        s[i] = 1f + 0.0000001f;
                    }
                }
                selected.transform.position.set(t[0], t[1], t[2]);
                selected.transform.scale.set(s[0], s[1], s[2]);
                selected.transform.rotation.set(Math.toRadians(r[0]), Math.toRadians(r[1]), Math.toRadians(r[2]));

                if (editTransformComponent == null) {
                    editTransformComponent = new EditTransformComponent(scene, selected.transform);
                }

                dragging = false;
            } else {
                if (editTransformComponent != null) {
                    scene.addUndoAction(editTransformComponent);
                    editTransformComponent = null;
                }
            }

        } else {
            ImGuizmo.setEnabled(false);
        }

        if (dragging) {
            DebugImpl.drawLine(
                    new Vector3f(startMouseCoords.x, startMouseCoords.y, 1),
                    new Vector3f(window.getMouseListener().getOrthoX(), startMouseCoords.y, 1),
                    selectionColor
            );
            DebugImpl.drawLine(
                    new Vector3f(startMouseCoords.x, startMouseCoords.y, 1),
                    new Vector3f(startMouseCoords.x, window.getMouseListener().getOrthoY(), 1),
                    selectionColor
            );
            DebugImpl.drawLine(
                    new Vector3f(window.getMouseListener().getOrthoX(), startMouseCoords.y, 1),
                    new Vector3f(window.getMouseListener().getOrthoX(), window.getMouseListener().getOrthoY(), 1),
                    selectionColor
            );
            DebugImpl.drawLine(
                    new Vector3f(startMouseCoords.x, window.getMouseListener().getOrthoY(), 1),
                    new Vector3f(window.getMouseListener().getOrthoX(), window.getMouseListener().getOrthoY(), 1),
                    selectionColor
            );
        }

        ImGui.end();
        ImGui.popStyleVar();
    }

    public GameObject getClicked() {
        if (scene.isPlaying()) return null;

        float x = window.getMouseListener().getOrthoX();
        float y = window.getMouseListener().getOrthoY();

        for (GameObject gameObject : scene.getGameObjects()) {
            Vector3f max = new Vector3f(0.5f, 0.5f, 1f);
            Vector3f min = new Vector3f(-0.5f, -0.5f, 1f);

            float tx = gameObject.transform.position.x + gameObject.transform.localPosition.x;
            float ty = gameObject.transform.position.y + gameObject.transform.localPosition.y;
            float tz = gameObject.transform.position.z + gameObject.transform.localPosition.z;
            Matrix4f model = gameObject.transform.getModel();

            Vector3f mX = max.mul(model.get3x3(new Matrix3d())).add(tx, ty, tz);
            Vector3f mN = min.mul(model.get3x3(new Matrix3d())).add(tx, ty, tz);

            if (x >= mN.x && x <= mX.x && y >= mN.y && y <= mX.y) {
                return gameObject;
            }
        }
        return null;
    }

    public Set<GameObject> getSelected(ImVec2 start, ImVec2 end) {
        if (scene.isPlaying()) return null;

        Set<GameObject> objects = new HashSet<>();
        float mnX = Math.min(start.x, end.x);
        float mxX = Math.max(start.x, end.x);
        float mnY = Math.min(start.y, end.y);
        float mxY = Math.max(start.y, end.y);
        for (GameObject gameObject : scene.getGameObjects()) {
            float minX = gameObject.transform.position.x - gameObject.transform.scale.x * 0.5f;
            float maxX = gameObject.transform.position.x + gameObject.transform.scale.x * 0.5f;
            float minY = gameObject.transform.position.y - gameObject.transform.scale.y * 0.5f;
            float maxY = gameObject.transform.position.y + gameObject.transform.scale.y * 0.5f;

            if (mnX <= minX && mnY <= minY && mxX >= maxX && mxY >= maxY) {
                objects.add(gameObject);
            }
        }

        return objects;
    }

    public boolean isMouseOnScene() {
        return window.getMouseListener().getX() >= leftX && window.getMouseListener().getX() <= rightX &&
               window.getMouseListener().getY() >= bottomY && window.getMouseListener().getY() <= topY;
    }

    private ImVec2 getCenteredPositionForViewport(ImVec2 aspectSize) {
        ImVec2 windowSize = getWindowAvailSize();

        float viewportX = (windowSize.x / 2f) - (aspectSize.x / 2f);
        float viewportY = (windowSize.y / 2f) - (aspectSize.y / 2f);

        return new ImVec2(viewportX + ImGui.getCursorPosX(), viewportY + ImGui.getCursorPosY());
    }

    private ImVec2 getWindowAvailSize() {
        ImVec2 windowSize = new ImVec2();
        ImGui.getContentRegionAvail(windowSize);
        windowSize.x -= ImGui.getScrollX();
        windowSize.y -= ImGui.getScrollY();
        return windowSize;
    }

    private ImVec2 getLargestAspectRatioViewport() {
        ImVec2 windowSize = getWindowAvailSize();
        float aspectWidth = windowSize.x;
        float aspectHeight = aspectWidth / getRatio();
        if (aspectHeight > windowSize.y) {
            aspectHeight = windowSize.y;
            aspectWidth = aspectHeight * getRatio();
        }
        return new ImVec2(aspectWidth, aspectHeight);
    }

    private float getRatio() {
        return 16 / 9f;
    }
}
