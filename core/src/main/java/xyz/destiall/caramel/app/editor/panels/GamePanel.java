package xyz.destiall.caramel.app.editor.panels;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import caramel.api.Application;
import caramel.api.Time;
import xyz.destiall.caramel.app.ApplicationImpl;
import caramel.api.objects.SceneImpl;

public final class GamePanel extends Panel {
    private final ApplicationImpl window;
    private ImVec2 windowSize;
    private ImVec2 windowPos;
    private float previousFps;
    private float leftX, rightX, topY, bottomY;

    public GamePanel(SceneImpl scene) {
        super(scene);
        window = ApplicationImpl.getApp();
    }

    @Override
    public void __imguiLayer() {
        ImGui.begin("Game", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse | ImGuiWindowFlags.AlwaysAutoResize);
        Panel.setPanelFocused(GamePanel.class, ImGui.isWindowFocused());
        Panel.setPanelHovered(GamePanel.class, ImGui.isWindowHovered());

        if (Time.isSecond) {
            previousFps = Time.getFPS();
        }
        ImGui.text("FPS: " + previousFps);

        windowSize = getLargestSizeForViewport();
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

        int texId = window.getGameViewFramebuffer().getTexture().getTexId();

        ImGui.image(texId, windowSize.x, windowSize.y, 0, 1, 1, 0);
        ImGui.end();
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
