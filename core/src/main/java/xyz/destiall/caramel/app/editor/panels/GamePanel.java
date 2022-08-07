package xyz.destiall.caramel.app.editor.panels;

import caramel.api.Time;
import caramel.api.objects.SceneImpl;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector2f;
import xyz.destiall.caramel.app.ApplicationImpl;

public final class GamePanel extends Panel {
    private float previousFps;

    public GamePanel(SceneImpl scene) {
        super(scene);
    }

    @Override
    public void __imguiLayer() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f);
        int flags = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse | ImGuiWindowFlags.AlwaysAutoResize;
        if (window.getScriptManager().isRecompiling()) {
            flags |= ImGuiWindowFlags.NoInputs | ImGuiWindowFlags.NoMouseInputs;
        }
        if (ImGui.begin("Game", flags)) {
            Panel.setPanelFocused(GamePanel.class, ImGui.isWindowFocused());
            Panel.setPanelHovered(GamePanel.class, ImGui.isWindowHovered());

            if (Time.isSecond) {
                previousFps = Time.getFPS();
            }
            ImGui.text("FPS: " + previousFps);

            ImVec2 windowSize = getLargestAspectRatioViewport();
            ImVec2 windowPos = getCenteredPositionForViewport(windowSize);

            ImGui.setCursorPos(windowPos.x, windowPos.y);

            ImVec2 bottomLeft = new ImVec2();
            ImGui.getCursorScreenPos(bottomLeft);
            bottomLeft.x -= ImGui.getScrollX();
            bottomLeft.y -= ImGui.getScrollY();

            float leftX = bottomLeft.x;
            float bottomY = bottomLeft.y;

            int texId = window.getGameViewFramebuffer().getTexture().getTexId();

            ImGui.image(texId, windowSize.x, windowSize.y, 0, 1, 1, 0);

            window.getMouseListener().setGameViewportPos(new Vector2f(leftX, bottomY));
            window.getMouseListener().setGameViewportSize(new Vector2f(windowSize.x, windowSize.y));

        }
        ImGui.end();
        ImGui.popStyleVar();
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
