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
import xyz.destiall.caramel.api.Application;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.api.components.EditorCamera;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.objects.SceneImpl;
import xyz.destiall.caramel.app.ApplicationImpl;

import java.util.Arrays;

public final class ScenePanel extends Panel {
    private final ApplicationImpl window;
    private ImVec2 windowSize = new ImVec2();
    private ImVec2 windowPos = new ImVec2();
    private float previousDt;
    private float leftX, rightX, topY, bottomY;

    public ScenePanel(SceneImpl scene) {
        super(scene);
        window = ApplicationImpl.getApp();
    }

    @Override
    public void __imguiLayer() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.begin("Scene", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse | ImGuiWindowFlags.AlwaysAutoResize);
        Panel.setPanelFocused(ScenePanel.class, ImGui.isWindowFocused());
        Panel.setPanelHovered(ScenePanel.class, ImGui.isWindowHovered());

        if (Time.isSecond) {
            previousDt = Time.deltaTime;
        }
        ImGui.text("Delta: " + previousDt + "ms");

        ImVec2 newWindowSize = getLargestSizeForViewport();
        if (!newWindowSize.equals(windowSize)) {
            windowSize = newWindowSize;
            window.getSceneViewFramebuffer().resize((int) windowSize.x, (int) windowSize.y);
        }

        windowPos = getCenteredPositionForViewport(windowSize);
        ImGui.setCursorPos(windowPos.x, windowPos.y);

        ImVec2 bottomLeft = new ImVec2();
        ImGui.getCursorScreenPos(bottomLeft);
        bottomLeft.x -= ImGui.getScrollX();
        bottomLeft.y -= ImGui.getScrollY();

        leftX = bottomLeft.x;
        bottomY = bottomLeft.y;
        rightX = bottomLeft.x + windowSize.x;
        topY = bottomLeft.y + windowSize.y;

        int texId = window.getSceneViewFramebuffer().getTexture().getTexId();
        ImGui.image(texId, windowSize.x, windowSize.y, 0, 1, 1, 0);

        window.getMouseListener().setGameViewportPos(new Vector2f(leftX, bottomY));
        window.getMouseListener().setGameViewportSize(new Vector2f(windowSize.x, windowPos.y));

        GameObject selected = window.getCurrentScene().getSelectedGameObject().stream().findFirst().orElse(null);

        if (Input.isKeyPressed(Input.Key.F) && selected != null &&
            (Panel.isWindowFocused(ScenePanel.class) || Panel.isWindowFocused(HierarchyPanel.class))) {
            scene.getEditorCamera().transform.position.x = selected.transform.position.x;
            scene.getEditorCamera().transform.position.y = selected.transform.position.y;
        }

        if (selected != null && !scene.isPlaying()) {
            ImGuizmo.setEnabled(true);
            ImGuizmo.setOrthographic(true);
            ImGuizmo.setDrawList();

            ImGuizmo.setRect(leftX, bottomY, windowSize.x, windowSize.y);

            EditorCamera camera = scene.getEditorCamera();
            Matrix4f inverseView = camera.getInverseView();
            Matrix4f projection = camera.getProjection();
            Matrix4f transform = selected.transform.model;

            float[] view = new float[16];
            inverseView.get(view);
            float[] proj = new float[16];
            projection.get(proj);
            float[] model = new float[16];
            transform.get(model);

            ImGuizmo.manipulate(view, proj, model, Operation.TRANSLATE, Mode.LOCAL);
            float[] t = new float[3];
            float[] r = new float[3];
            float[] s = new float[3];
            ImGuizmo.decomposeMatrixToComponents(model, t, r, s);
            if (ImGuizmo.isUsing()) {
                boolean nan = false;
                for (Float f : model) {
                    if (Float.isNaN(f) || Float.isInfinite(f)) {
                        nan = true;
                        System.out.println(Arrays.toString(model));
                        break;
                    }
                }
                if (!nan) {
                    selected.transform.position.set(t[0], t[1], t[2]);
                    selected.transform.rotation.set(r[0], r[1], r[2]);
                    selected.transform.scale.set(s[0], s[1], s[2]);
                }
            }

        } else {
            ImGuizmo.setEnabled(false);
        }

        ImGui.end();
        ImGui.popStyleVar();
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

    private ImVec2 getLargestSizeForViewport() {
        ImVec2 windowSize = getWindowAvailSize();
        return new ImVec2(windowSize.x, windowSize.y);
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
