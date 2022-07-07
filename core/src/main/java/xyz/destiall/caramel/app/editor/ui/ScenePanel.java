package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Application;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.api.components.EditorCamera;
import xyz.destiall.caramel.api.objects.SceneImpl;
import xyz.destiall.caramel.app.editor.debug.DebugDraw;
import xyz.destiall.caramel.app.utils.Payload;

import java.util.HashSet;
import java.util.Set;

public final class ScenePanel extends Panel {
    private final ApplicationImpl window;
    private final ImVec2 startMouseCoords = new ImVec2(0, 0);
    private boolean dragging = false;
    private ImVec2 windowSize;
    private ImVec2 windowPos;
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

        windowSize = getLargestAspectRatioViewport();
        windowPos = getCenteredPositionForViewport(windowSize);

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

        window.getMouseListener().setGameViewportPos(new Vector2f(leftX, bottomY));
        window.getMouseListener().setGameViewportSize(new Vector2f(windowSize.x, windowSize.y));

        if (ImGui.isMouseDown(Input.Mouse.LEFT) && ImGui.isWindowHovered() && !dragging) {
            dragging = true;
            startMouseCoords.set(window.getMouseListener().getOrthoX(), window.getMouseListener().getOrthoY());
        }

        if (Input.isMousePressed(Input.Mouse.LEFT) && ImGui.isWindowFocused() && ImGui.isWindowHovered()) {
            GameObject clicked = getClicked();
            if (clicked != null) {
                if (!Input.isKeyDown(Input.Key.CONTROL)) {
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
                if (!Input.isKeyDown(Input.Key.CONTROL)) {
                    window.getCurrentScene().getSelectedGameObject().clear();
                }
                window.getCurrentScene().getSelectedGameObject().addAll(objects);
            }
            dragging = false;
        }

        if (ImGui.isWindowFocused()) {
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
                }

                if (Input.isControlPressedAnd(Input.Key.D) && !scene.getSelectedGameObject().isEmpty()) {
                    for (GameObject copied : scene.getSelectedGameObject()) {
                        GameObject go = copied.clone(false);
                        scene.addGameObject(go);
                    }
                }
            }

            if (Input.isControlPressedAnd(Input.Key.A)) {
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
            Matrix4f transform = selected.transform.model;

            float[] view = new float[16];
            inverseView.get(view);
            float[] proj = new float[16];
            projection.get(proj);
            float[] model = new float[16];
            transform.get(model);

            ImGuizmo.manipulate(view, proj, model, Operation.TRANSLATE, Mode.LOCAL);

            if (ImGuizmo.isUsing()) {
                float[] t = new float[3];
                float[] r = new float[3];
                float[] s = new float[3];
                ImGuizmo.decomposeMatrixToComponents(model, t, r, s);
                selected.transform.position.set(t[0], t[1], t[2]);
                selected.transform.scale.set(s[0], s[1], s[2]);
                dragging = false;
            }

        } else {
            ImGuizmo.setEnabled(false);
        }

        if (dragging) {
            DebugDraw.INSTANCE.addLine(
                    new Vector3f(startMouseCoords.x, startMouseCoords.y, 1),
                    new Vector3f(window.getMouseListener().getOrthoX(), startMouseCoords.y, 1),
                    new Vector3f(1.f, 0.f, 0.f)
            );
            DebugDraw.INSTANCE.addLine(
                    new Vector3f(startMouseCoords.x, startMouseCoords.y, 1),
                    new Vector3f(startMouseCoords.x, window.getMouseListener().getOrthoY(), 1),
                    new Vector3f(1.f, 0.f, 0.f)
            );
            DebugDraw.INSTANCE.addLine(
                    new Vector3f(window.getMouseListener().getOrthoX(), startMouseCoords.y, 1),
                    new Vector3f(window.getMouseListener().getOrthoX(), window.getMouseListener().getOrthoY(), 1),
                    new Vector3f(1.f, 0.f, 0.f)
            );
            DebugDraw.INSTANCE.addLine(
                    new Vector3f(startMouseCoords.x, window.getMouseListener().getOrthoY(), 1),
                    new Vector3f(window.getMouseListener().getOrthoX(), window.getMouseListener().getOrthoY(), 1),
                    new Vector3f(1.f, 0.f, 0.f)
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
            float minX = gameObject.transform.position.x - gameObject.transform.scale.x * 0.5f;
            float maxX = gameObject.transform.position.x + gameObject.transform.scale.x * 0.5f;
            float minY = gameObject.transform.position.y - gameObject.transform.scale.y * 0.5f;
            float maxY = gameObject.transform.position.y + gameObject.transform.scale.y * 0.5f;

            if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
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
        return Application.getApp().getWidth() / (float) Application.getApp().getHeight();
    }

    public ImVec2 getWindowPos() {
        return windowPos;
    }

    public ImVec2 getWindowSize() {
        return windowSize;
    }
}
