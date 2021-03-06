package xyz.destiall.caramel.app.ui;

import caramel.api.Input;
import caramel.api.Time;
import caramel.api.objects.SceneImpl;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.flag.ImGuiBackendFlags;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.type.ImBoolean;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.editor.debug.DebugLine;
import xyz.destiall.caramel.app.editor.panels.GamePanel;
import xyz.destiall.caramel.app.editor.panels.Panel;
import xyz.destiall.caramel.app.editor.panels.ScenePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_3;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_4;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_5;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetJoystickCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

public final class ImGUILayer {
    private final ApplicationImpl window;
    private final long glfwWindow;
    private ScenePanel scenePanel;
    private GamePanel gamePanel;

    public final List<DebugLine> lines = new ArrayList<>();

    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public ImGUILayer(ApplicationImpl window) {
        this.window = window;
        this.glfwWindow = window.glfwWindow;
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

        io.setKeyMap(ImGuiKey.Backspace, Input.Key.BACKSPACE);
        io.setKeyMap(ImGuiKey.Enter, Input.Key.ENTER);
        io.setKeyMap(ImGuiKey.Delete, Input.Key.DELETE);
        io.setKeyMap(ImGuiKey.DownArrow, Input.Key.DOWN);
        io.setKeyMap(ImGuiKey.UpArrow, Input.Key.UP);
        io.setKeyMap(ImGuiKey.LeftArrow, Input.Key.LEFT);
        io.setKeyMap(ImGuiKey.RightArrow, Input.Key.RIGHT);
        io.setKeyMap(ImGuiKey.End, Input.Key.END);
        io.setKeyMap(ImGuiKey.Insert, Input.Key.INSERT);
        io.setKeyMap(ImGuiKey.PageDown, Input.Key.PAGE_DOWN);
        io.setKeyMap(ImGuiKey.PageUp, Input.Key.PAGE_UP);
        io.setKeyMap(ImGuiKey.Home, Input.Key.HOME);
        io.setKeyMap(ImGuiKey.Tab, Input.Key.TAB);
        io.setKeyMap(ImGuiKey.Escape, Input.Key.ESCAPE);
        io.setKeyMap(ImGuiKey.A, Input.Key.A);
        io.setKeyMap(ImGuiKey.C, Input.Key.C);
        io.setKeyMap(ImGuiKey.V, Input.Key.V);
        io.setKeyMap(ImGuiKey.Y, Input.Key.Y);
        io.setKeyMap(ImGuiKey.X, Input.Key.X);
        io.setKeyMap(ImGuiKey.Z, Input.Key.Z);
        io.setKeyMap(ImGuiKey.KeyPadEnter, Input.Key.ENTER);

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

            if (window.getCurrentScene().isPlaying() && window.isFocused()) {
                window.getKeyListener().keyCallback(w, key, scancode, action, mods);
            }
        });

        glfwSetJoystickCallback(window.getJoystickListener()::joystickCallback);

        glfwSetCharCallback(glfwWindow, (w, c) -> io.addInputCharacter(c));

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

            if (window.getCurrentScene().isPlaying() && window.isFocused())
                window.getMouseListener().mouseButtonCallback(w, button, action, mods);
        });

        glfwSetScrollCallback(glfwWindow, (w, xOffset, yOffset) -> {
            io.setMouseWheelH(io.getMouseWheelH() + (float) xOffset);
            io.setMouseWheel(io.getMouseWheel() + (float) yOffset);

            if (window.getCurrentScene().isPlaying() && window.isFocused())
                window.getMouseListener().mouseScrollCallback(w, xOffset, yOffset);
        });

        glfwSetCursorPosCallback(glfwWindow, (w, x, y) -> {
            io.setMousePos((float) x, (float) y);

            if (window.getCurrentScene().isPlaying() && window.isFocused())
                window.getMouseListener().mousePosCallback(w, x, y);
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
                return clipboardString != null ? clipboardString : "";
            }
        });

        imGuiGl3.init("#version 330 core");
    }

    public void update() {
        startFrame(Time.deltaTime);

        if (!window.isFullscreen()) {
            setupDockspace();
            window.getCurrentScene().__imguiLayer();
        }

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

    private void startFrame(final float deltaTime) {
        float[] winWidth = { ApplicationImpl.getApp().getWidth() };
        float[] winHeight = { ApplicationImpl.getApp().getHeight() };
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
        ImGuizmo.beginFrame();
        Panel.reset();
    }

    private void endFrame() {
        for (int i = 0; i < lines.size(); i++) {
            DebugLine l = lines.get(i);
            if (l.beginFrame() < 0) {
                lines.remove(i);
                i--;
                continue;
            }

            ImGui.getForegroundDrawList().addCircle(l.from.x, l.from.y, 5f, Color.red.getRGB());
        }

        if (!window.isFullscreen()) {
            ImGui.end();
        }
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        ImGui.updatePlatformWindows();
        ImGui.renderPlatformWindowsDefault();
        glfwMakeContextCurrent(window.glfwWindow);
    }

    public void destroyImGui() {
        imGuiGl3.dispose();
        ImGui.getIO().getFonts().destroy();
        ImGui.destroyContext();
    }
}
