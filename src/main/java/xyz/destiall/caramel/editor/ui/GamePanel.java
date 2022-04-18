package xyz.destiall.caramel.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.editor.Scene;
import xyz.destiall.caramel.editor.Time;
import xyz.destiall.caramel.objects.GameObject;

import static org.lwjgl.glfw.GLFW.*;

public class GamePanel extends Panel {
    private final Application window;
    private ImVec2 gameWindowSize;
    private ImVec2 gameWindowPos;
    private float previousFps;
    private float previousDt;
    private float leftX, rightX, topY, bottomY;
    private int gizmoOperation;

    public GamePanel(Scene scene) {
        super(scene);
        window = Application.getApp();
    }

    @Override
    public void imguiLayer() {
        ImGui.begin("Game", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
        if (Time.isSecond) {
            previousFps = Time.getFPS();
            previousDt = Time.deltaTime;
        }
        ImGui.text("FPS: " + previousFps);
        ImGui.text("Delta: " + previousDt + "ms");

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

        int texId = window.getFramebuffer().getTexture().getTexId();
        ImGui.image(texId, gameWindowSize.x, gameWindowSize.y, 0, 1, 1, 0);

        window.getMouseListener().setGameViewportPos(new Vector2f(topLeft.x, topLeft.y));
        window.getMouseListener().setGameViewportSize(new Vector2f(gameWindowSize.x, gameWindowPos.y));

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

    private void editTransform() {
        if (ImGui.isKeyPressed(GLFW_KEY_T)) {
            gizmoOperation = Operation.TRANSLATE;
        } else if (ImGui.isKeyPressed(GLFW_KEY_R)) {
            gizmoOperation = Operation.ROTATE;
        } else if (ImGui.isKeyPressed(GLFW_KEY_S)) {
            gizmoOperation = Operation.SCALE;
        }

        GameObject selected = window.getCurrentScene().getSelectedGameObject();
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

        ImGui.inputFloat3("Tr", position, "%.3f", ImGuiInputTextFlags.ReadOnly);
        ImGui.inputFloat3("Rt", rotation, "%.3f", ImGuiInputTextFlags.ReadOnly);
        ImGui.inputFloat3("Sc", scale, "%.3f", ImGuiInputTextFlags.ReadOnly);

        if (ImGuizmo.isUsing()) {
            ImGuizmo.recomposeMatrixFromComponents(model, position, rotation, scale);
            selected.transform.model.set(model);
            selected.transform.position.set(position);
            selected.transform.position.set(position);
            selected.transform.position.set(position);
        }
    }

    public ImVec2 getGameWindowPos() {
        return gameWindowPos;
    }

    public ImVec2 getGameWindowSize() {
        return gameWindowSize;
    }
}
