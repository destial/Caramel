package xyz.destiall.caramel.app.ui;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.type.ImBoolean;
import org.joml.Vector2f;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.editor.Time;

import static org.lwjgl.glfw.GLFW.*;

public class ImGUILayer {
    private ImVec2 gameWindowSize;
    private ImVec2 gameWindowPos;
    private final Application window;
    private final long glfwWindow;
    private float leftX, rightX, topY, bottomY;

    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public ImGUILayer(Application window) {
        this.window = window;
        this.glfwWindow = window.glfwWindow;
    }

    public ImVec2 getGameWindowPos() {
        return gameWindowPos;
    }

    public ImVec2 getGameWindowSize() {
        return gameWindowSize;
    }

    // Initialize Dear ImGui.
    public void initImGui() {
        ImGui.createContext();

        final ImGuiIO io = ImGui.getIO();

        io.setIniFilename("imgui.ini");
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.addBackendFlags(ImGuiBackendFlags.HasMouseCursors);
        io.setBackendPlatformName("imgui_java_impl_glfw");

        glfwSetKeyCallback(glfwWindow, (w, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                io.setKeysDown(key, true);
            } else if (action == GLFW_RELEASE) {
                io.setKeysDown(key, false);
            }

            io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
            io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
            io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
            io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));

            //if (!io.getWantCaptureKeyboard()) {
                window.getKeyListener().keyCallback(w, key, scancode, action, mods);
            //}
        });

        glfwSetCharCallback(glfwWindow, (w, c) -> {
            if (c != GLFW_KEY_DELETE) {
                io.addInputCharacter(c);
            }
        });

        glfwSetMouseButtonCallback(glfwWindow, (w, button, action, mods) -> {
            final boolean[] mouseDown = new boolean[5];

            mouseDown[0] = button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE;
            mouseDown[1] = button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE;
            mouseDown[2] = button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE;
            mouseDown[3] = button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE;
            mouseDown[4] = button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE;

            io.setMouseDown(mouseDown);

            if (!io.getWantCaptureMouse() && mouseDown[1]) {
                ImGui.setWindowFocus(null);
            }

            if (!io.getWantCaptureMouse() || isMouseOnGameViewport())
                window.getMouseListener().mouseButtonCallback(w, button, action, mods);
        });

        glfwSetScrollCallback(glfwWindow, (w, xOffset, yOffset) -> {
            io.setMouseWheelH(io.getMouseWheelH() + (float) xOffset);
            io.setMouseWheel(io.getMouseWheel() + (float) yOffset);

            if (!io.getWantCaptureMouse() || isMouseOnGameViewport())
                window.getMouseListener().mouseScrollCallback(w, xOffset, yOffset);
        });

        io.setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(final String s) {
                glfwSetClipboardString(glfwWindow, s);
            }
        });

        io.setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                final String clipboardString = glfwGetClipboardString(glfwWindow);
                if (clipboardString != null) {
                    return clipboardString;
                } else {
                    return "";
                }
            }
        });

        imGuiGl3.init("#version 330 core");
    }

    public void update() {
        startFrame(Time.deltaTime);

        setupDockspace();
        window.getCurrentScene().imguiLayer();
        setupViewport();

        endFrame();
    }

    private void setupDockspace() {
        int windowFlags =
                ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoBringToFrontOnFocus |
                ImGuiWindowFlags.NoNavFocus;

        ImGui.setNextWindowPos(0f, 0f, ImGuiCond.Always);
        ImGui.setNextWindowSize(window.getWidth(), window.getHeight());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);

        ImGui.begin("Dockspace", new ImBoolean(true), windowFlags);
        ImGui.popStyleVar(2);

        ImGui.dockSpace(ImGui.getID("Dockspace"));
    }

    private float previousFps;
    private float previousDt;

    private void setupViewport() {
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

    public boolean isMouseOnGameViewport() {
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

    private void startFrame(final float deltaTime) {
        float[] winWidth = { Application.getApp().getWidth() };
        float[] winHeight = { Application.getApp().getHeight() };
        double[] mousePosX = { 0 };
        double[] mousePosY = { 0 };
        glfwGetCursorPos(glfwWindow, mousePosX, mousePosY);

        final ImGuiIO io = ImGui.getIO();
        io.setDisplaySize(winWidth[0], winHeight[0]);
        io.setDisplayFramebufferScale(1f, 1f);
        io.setMousePos((float) mousePosX[0], (float) mousePosY[0]);
        io.setDeltaTime(deltaTime);

        glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        ImGui.newFrame();
    }

    private void endFrame() {
        ImGui.end();
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        ImGui.updatePlatformWindows();
        ImGui.renderPlatformWindowsDefault();
        glfwMakeContextCurrent(window.glfwWindow);
    }

    public void destroyImGui() {
        imGuiGl3.dispose();
        ImGui.destroyContext();
    }
}
