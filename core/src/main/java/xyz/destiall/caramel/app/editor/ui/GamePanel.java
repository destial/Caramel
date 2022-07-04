package xyz.destiall.caramel.app.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.editor.SceneImpl;

public final class GamePanel extends Panel {
    private final ApplicationImpl window;
    private ImVec2 gameWindowSize;
    private ImVec2 gameWindowPos;
    private float leftX, rightX, topY, bottomY;

    public GamePanel(SceneImpl scene) {
        super(scene);
        window = ApplicationImpl.getApp();
    }

    @Override
    public void __imguiLayer() {
        ImGui.begin("Game", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
        Panel.setPanelFocused(GamePanel.class, ImGui.isWindowFocused());
        Panel.setPanelHovered(GamePanel.class, ImGui.isWindowHovered());

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

        int texId = window.getGameViewFramebuffer().getTexture().getTexId();

        ImGui.image(texId, gameWindowSize.x, gameWindowSize.y, 0, 1, 1, 0);
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

    public ImVec2 getGameWindowPos() {
        return gameWindowPos;
    }

    public ImVec2 getGameWindowSize() {
        return gameWindowSize;
    }
}
