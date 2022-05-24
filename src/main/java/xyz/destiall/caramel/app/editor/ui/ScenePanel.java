package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.app.editor.Scene;
import xyz.destiall.caramel.app.editor.EditorCamera;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;

public class ScenePanel extends Panel {
    private final Application window;
    private ImVec2 gameWindowSize;
    private ImVec2 gameWindowPos;
    private float previousFps;
    private float previousDt;
    private float leftX, rightX, topY, bottomY;
    private int gizmoOperation;
    private ImBoolean showImGuizmoWindow = new ImBoolean(true);

    public ScenePanel(Scene scene) {
        super(scene);
        window = Application.getApp();
    }

    @Override
    public void imguiLayer() {
        ImGui.begin("Scene", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
        if (Time.isSecond) {
            previousFps = Time.getFPS();
            previousDt = Time.deltaTime;
        }
        ImGui.text("FPS: " + previousFps);
        ImGui.text("Delta: " + previousDt + "ms");

        Panel.setPanelFocused(ScenePanel.class, ImGui.isWindowFocused());
        Panel.setPanelHovered(ScenePanel.class, ImGui.isWindowHovered());

        gameWindowSize = getLargestSizeForViewport();
        gameWindowPos = getCenteredPositionForViewport(gameWindowSize);

        ImGui.setCursorPos(gameWindowPos.x, gameWindowPos.y);

        ImVec2 topLeft = new ImVec2();
        ImGui.getCursorScreenPos(topLeft);
        topLeft.x -= ImGui.getScrollX();
        topLeft.y -= ImGui.getScrollY();

        leftX = topLeft.x;
        bottomY = topLeft.y;
        rightX = topLeft.x + gameWindowSize.x;
        topY = topLeft.y + gameWindowSize.y;

        int texId = window.getSceneViewFramebuffer().getTexture().getTexId();

        window.getMouseListener().setGameViewportPos(new Vector2f(topLeft.x, topLeft.y));
        window.getMouseListener().setGameViewportSize(new Vector2f(gameWindowSize.x, gameWindowPos.y));

        ImGui.image(texId, gameWindowSize.x, gameWindowSize.y, 0, 1, 1, 0);

        GameObject selected = window.getCurrentScene().getSelectedGameObject();

        if (selected != null && false) {
            ImGuizmo.beginFrame();
            ImGuizmo.setOrthographic(false);
            ImGuizmo.setDrawList();
            ImGuizmo.setRect(ImGui.getWindowPosX(), ImGui.getWindowPosY(), ImGui.getWindowWidth(), ImGui.getWindowHeight());

            EditorCamera camera = scene.getEditorCamera();
            Matrix4f cameraProjection = new Matrix4f(camera.projection);
            Matrix4f cameraView = new Matrix4f(camera.transform.model);

            Matrix4f transform = selected.transform.model;
            float[] floats = transform.get(new float[16]);
            ImGuizmo.manipulate(cameraView.get(new float[16]), cameraProjection.get(new float[16]), floats, Operation.TRANSLATE, Mode.LOCAL);
            if (ImGuizmo.isUsing()) {
                selected.transform.position.set(Arrays.copyOf(floats, 3));
            }
        }

        if (ImGui.isWindowHovered()) {
            if (Input.isKeyPressed(Input.Key.F)) {

            }
        }

        ImGui.end();
    }

    private ImVec2 getCenteredPositionForViewport(ImVec2 aspectSize) {
        ImVec2 windowSize = new ImVec2();
        ImGui.getContentRegionAvail(windowSize);
        windowSize.x -= ImGui.getScrollX();
        windowSize.y -= ImGui.getScrollY();

        float viewportX = (windowSize.x / 2f) - (aspectSize.x / 2f);
        float viewportY = (windowSize.y / 2f) - (aspectSize.y / 2f);

        return new ImVec2(viewportX, viewportY);
    }

    public boolean isMouseOnScene() {
        return window.getMouseListener().getX() >= leftX && window.getMouseListener().getX() <= rightX &&
                window.getMouseListener().getY() >= bottomY && window.getMouseListener().getY() <= topY;
    }

    private ImVec2 getLargestSizeForViewport() {
        ImVec2 windowSize = new ImVec2();
        ImGui.getContentRegionAvail(windowSize);
        windowSize.x -= ImGui.getScrollX();
        windowSize.y -= ImGui.getScrollY();

        float aspectWidth = windowSize.x;
        float aspectHeight = aspectWidth / (16 / 9f);
        if (aspectHeight > windowSize.y) {
            aspectHeight = windowSize.y;
            aspectWidth = aspectHeight * (16 / 9f);
        }
        return new ImVec2(aspectWidth, aspectHeight);
    }

    private void editTransform(ImBoolean showImGuizmoWindow) {
        if (ImGui.isKeyPressed(GLFW_KEY_T)) {
            gizmoOperation = Operation.TRANSLATE;
        } else if (ImGui.isKeyPressed(GLFW_KEY_R)) {
            gizmoOperation = Operation.ROTATE;
        } else if (ImGui.isKeyPressed(GLFW_KEY_S)) {
            gizmoOperation = Operation.SCALE;
        }

        GameObject selected = window.getCurrentScene().getSelectedGameObject();
        if (selected == null) return;

        float[] model = selected.transform.model.get(new float[16]);
        float[] position = {
                selected.transform.position.x + selected.transform.localPosition.x,
                selected.transform.position.y + selected.transform.localPosition.y,
                selected.transform.position.z + selected.transform.localPosition.z,
        };
        Vector3f euler = selected.transform.rotation.getEulerAnglesXYZ(new Vector3f());
        Vector3f localEuler = selected.transform.localRotation.getEulerAnglesXYZ(new Vector3f());
        float[] rotation = {
                euler.x + localEuler.x,
                euler.y + localEuler.y,
                euler.z + localEuler.z,
        };
        float[] scale = {
                selected.transform.scale.x * selected.transform.localScale.x,
                selected.transform.scale.y * selected.transform.localScale.y,
                selected.transform.scale.z * selected.transform.localScale.z,
        };
        if (ImGuizmo.isUsing()) {
            ImGuizmo.decomposeMatrixToComponents(model, position, rotation, scale);
        }

        ImGui.inputFloat3("Tr", position, "%.3f", ImGuiInputTextFlags.None);
        ImGui.inputFloat3("Rt", rotation, "%.3f", ImGuiInputTextFlags.None);
        ImGui.inputFloat3("Sc", scale, "%.3f", ImGuiInputTextFlags.None);

        if (ImGuizmo.isUsing()) {
            ImGuizmo.recomposeMatrixFromComponents(model, position, rotation, scale);
            selected.transform.position.set(position);
            // selected.transform.rotation.rotation);
            selected.transform.scale.set(scale);
        }
    }

    public ImVec2 getGameWindowPos() {
        return gameWindowPos;
    }

    public ImVec2 getGameWindowSize() {
        return gameWindowSize;
    }
}
