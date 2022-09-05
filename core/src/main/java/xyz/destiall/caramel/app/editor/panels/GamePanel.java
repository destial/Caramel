package xyz.destiall.caramel.app.editor.panels;

import caramel.api.Time;
import caramel.api.objects.SceneImpl;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector2f;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.scripts.EditorScriptManager;

public final class GamePanel extends Panel {
    private float previousFps;

    public GamePanel(final SceneImpl scene) {
        super(scene);
    }

    @Override
    public void __imguiLayer() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f);
        int flags = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse | ImGuiWindowFlags.AlwaysAutoResize;
        if (window.getScriptManager() instanceof EditorScriptManager && ((EditorScriptManager) window.getScriptManager()).isRecompiling()) {
            flags |= ImGuiWindowFlags.NoInputs | ImGuiWindowFlags.NoMouseInputs;
        }
        if (ImGui.begin("Game", flags)) {
            Panel.setPanelFocused(GamePanel.class, ImGui.isWindowFocused());
            Panel.setPanelHovered(GamePanel.class, ImGui.isWindowHovered());

            if (Time.isSecond) {
                previousFps = Time.getFPS();
            }
            ImGui.text("FPS: " + previousFps);

            final ImVec2 windowSize = getLargestAspectRatioViewport();
            final ImVec2 windowPos = getCenteredPositionForViewport(windowSize);

            ImGui.setCursorPos(windowPos.x, windowPos.y);

            final ImVec2 bottomLeft = new ImVec2();
            ImGui.getCursorScreenPos(bottomLeft);
            bottomLeft.x -= ImGui.getScrollX();
            bottomLeft.y -= ImGui.getScrollY();

            final float leftX = bottomLeft.x;
            final float bottomY = bottomLeft.y;

            final int texId = window.getGameViewFramebuffer().getTexture().getTexId();

            ImGui.image(texId, windowSize.x, windowSize.y, 0, 1, 1, 0);

            window.getMouseListener().setGameViewportPos(new Vector2f(leftX, bottomY));
            window.getMouseListener().setGameViewportSize(new Vector2f(windowSize.x, windowSize.y));

        }
        ImGui.end();
        ImGui.popStyleVar();
    }

    private ImVec2 getCenteredPositionForViewport(final ImVec2 aspectSize) {
        final ImVec2 windowSize = getWindowAvailSize();

        float viewportX = (windowSize.x / 2f) - (aspectSize.x / 2f);
        float viewportY = (windowSize.y / 2f) - (aspectSize.y / 2f);

        return new ImVec2(viewportX + ImGui.getCursorPosX(), viewportY + ImGui.getCursorPosY());
    }

    private ImVec2 getWindowAvailSize() {
        final ImVec2 windowSize = new ImVec2();
        ImGui.getContentRegionAvail(windowSize);
        windowSize.x -= ImGui.getScrollX();
        windowSize.y -= ImGui.getScrollY();
        return windowSize;
    }

    private ImVec2 getLargestAspectRatioViewport() {
        final ImVec2 windowSize = getWindowAvailSize();
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
