package xyz.destiall.caramel.app;

import caramel.api.Application;
import caramel.api.Component;
import caramel.api.Input;
import caramel.api.input.JoystickListener;
import caramel.api.Time;
import caramel.api.components.Camera;
import caramel.api.graphics.Graphics;
import caramel.api.graphics.opengl.OpenGL30;
import caramel.api.scripts.ScriptManager;
import caramel.api.sound.decoder.AudioDecoder;
import caramel.api.utils.SystemIO;
import org.lwjgl.glfw.*;
import xyz.destiall.caramel.app.editor.EditorSceneLoader;
import xyz.destiall.caramel.app.editor.managers.AudioManager;
import caramel.api.components.EditorCamera;
import caramel.api.components.Transform;
import caramel.api.components.UICamera;
import caramel.api.debug.Debug;
import caramel.api.debug.DebugImpl;
import caramel.api.events.FullscreenEvent;
import caramel.api.events.WindowFocusEvent;
import caramel.api.objects.Scene;
import caramel.api.objects.SceneImpl;
import caramel.api.render.BatchRenderer;
import caramel.api.render.MeshRenderer;
import caramel.api.render.Shader;
import caramel.api.render.Text;
import caramel.api.sound.SoundSource;
import caramel.api.texture.Texture;
import caramel.api.utils.FileIO;
import imgui.ImGui;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;
import org.reflections.Reflections;
import xyz.destiall.caramel.app.editor.debug.DebugDraw;
import xyz.destiall.caramel.app.editor.managers.BodyManager;
import xyz.destiall.caramel.app.editor.managers.MeshManager;
import xyz.destiall.caramel.app.runtime.RuntimeSceneLoader;
import xyz.destiall.caramel.app.scripts.EditorScriptManager;
import xyz.destiall.caramel.app.runtime.RuntimeScriptManager;
import xyz.destiall.caramel.app.serialize.SceneSerializer;
import xyz.destiall.caramel.app.ui.ImGUILayer;
import xyz.destiall.caramel.app.ui.ImGuiUtils;
import xyz.destiall.caramel.app.utils.Payload;
import xyz.destiall.java.events.EventHandling;
import xyz.destiall.java.events.Listener;
import xyz.destiall.java.gson.Gson;
import xyz.destiall.java.gson.GsonBuilder;
import xyz.destiall.java.gson.JsonArray;
import xyz.destiall.java.gson.JsonElement;
import xyz.destiall.java.gson.JsonObject;
import xyz.destiall.java.timer.Scheduler;
import xyz.destiall.java.timer.Task;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static caramel.api.graphics.GL20.*;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class ApplicationImpl extends Application implements Runnable {
    public boolean EDITOR_MODE = true;
    private boolean running = true;

    private final MouseListenerImpl mouseListener;
    private final KeyListenerImpl keyListener;
    private final JoystickListenerImpl joystickListener;
    private final EventHandling eventHandler;
    private final Scheduler scheduler;
    private final List<Listener> listeners;
    private final Gson serializer;

    private ImGUILayer imGui;
    private Framebuffer[] framebuffer;
    private ScriptManager scriptManager;

    private int width;
    private int height;
    private int winPosX;
    private int winPosY;
    private boolean fullscreen;
    private List<String> lastScenes;
    private String title;
    private boolean focused;

    private long audioContext;
    private long audioDevice;

    private SceneLoader sceneLoader;
    private JsonObject settings;

    public long glfwWindow;

    public static ApplicationImpl getRuntime() {
        if (inst == null) inst = new ApplicationImpl();
        ((ApplicationImpl) inst).EDITOR_MODE = false;
        return (ApplicationImpl) inst;
    }

    public static ApplicationImpl getApp() {
        if (inst == null) inst = new ApplicationImpl();
        return (ApplicationImpl) inst;
    }

    private ApplicationImpl() {
        title = "Caramel";
        mouseListener = new MouseListenerImpl();
        keyListener = new KeyListenerImpl();
        joystickListener = new JoystickListenerImpl();
        eventHandler = new EventHandling();
        scheduler = new Scheduler();
        listeners = new ArrayList<>();
        listeners.add(new AudioManager());
        listeners.add(new MeshManager());
        listeners.add(new BodyManager());
        serializer = new GsonBuilder()
                .registerTypeAdapter(SceneImpl.class, new SceneSerializer())
                .serializeNulls()
                .setPrettyPrinting()
                .setLenient()
                .create();
        focused = true;
    }

    @Override
    public void run() {
        setup();
        if (init()) loop();
        destroy();
    }

    private void setup() {
        // Load settings and data files
        sceneLoader = EDITOR_MODE ? new EditorSceneLoader(this) : new RuntimeSceneLoader(this);

        try {
            if (EDITOR_MODE) {
                final File settings = new File("settings.json");
                this.settings = new JsonObject();
                if (settings.createNewFile()) {
                    this.settings.addProperty("width", width = 1920);
                    this.settings.addProperty("height", height = 1080);
                    this.settings.addProperty("windowPosX", winPosX = 25);
                    this.settings.addProperty("windowPosY", winPosY = 25);
                    this.settings.addProperty("fullscreen", fullscreen = false);
                    lastScenes = new ArrayList<>();
                    String lastScene = "assets/scenes/Untitled Scene.caramel";
                    lastScenes.add(lastScene);
                    JsonArray array = new JsonArray();
                    array.add(lastScene);
                    this.settings.add("lastScene", array);
                    this.settings.addProperty("imgui", ImGuiUtils.DEFAULT_SETTINGS);
                    FileIO.writeData(settings, serializer.toJson(this.settings));

                } else {
                    this.settings = serializer.fromJson(FileIO.readData(settings), JsonObject.class);
                    width = this.settings.get("width").getAsInt();
                    height = this.settings.get("height").getAsInt();
                    winPosX = this.settings.get("windowPosX").getAsInt();
                    winPosY = this.settings.get("windowPosY").getAsInt();
                    lastScenes = new ArrayList<>();
                    final JsonElement element = this.settings.get("lastScene");
                    if (element.isJsonArray()) {
                        JsonArray array = element.getAsJsonArray();
                        for (JsonElement loc : array) {
                            lastScenes.add(loc.getAsString());
                        }
                    } else {
                        // Legacy compatibility
                        final String loc = element.getAsString();
                        lastScenes.add(loc);
                    }
                    fullscreen = this.settings.has("fullscreen") && this.settings.get("fullscreen").getAsBoolean();
                }

                final File assets = new File("assets" + File.separator);
                if (!assets.exists() && assets.mkdir()) {
                    new File(assets, "models" + File.separator).mkdirs();
                    new File(assets, "textures" + File.separator).mkdirs();
                    new File(assets, "shaders" + File.separator).mkdirs();
                    new File(assets, "scenes" + File.separator).mkdirs();
                    new File(assets, "sounds" + File.separator).mkdirs();
                    new File(assets, "fonts" + File.separator).mkdirs();
                    FileIO.saveResource("arial.TTF", "assets/fonts/arial.TTF");
                }
            } else {
                fullscreen = true;
                final String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
                final File source = new File(path);

                final File config = new File("config.json");
                if (config.exists()) {
                    this.settings = serializer.fromJson(FileIO.readData(config), JsonObject.class);
                } else {
                    this.settings = serializer.fromJson(FileIO.readUTFFromZip(source, "config.json"), JsonObject.class);
                }
                width = this.settings.get("width").getAsInt();
                height = this.settings.get("height").getAsInt();
                winPosX = this.settings.get("windowPosX").getAsInt();
                winPosY = this.settings.get("windowPosY").getAsInt();
                final JsonArray array = this.settings.get("scenes").getAsJsonArray();
                lastScenes = new ArrayList<>();
                for (JsonElement loc : array) {
                    lastScenes.add(loc.getAsString());
                }

                final File assets = new File("assets" + File.separator);
                if (!assets.exists() && assets.mkdir()) {
                    FileIO.extract(source, FileIO.ROOT_FILE, "assets");
                }
            }

            final File logo16 = new File("logo_16.png");
            if (!logo16.exists()) {
                FileIO.saveResource("logo_16.png", "logo_16.png");
            }

            final File logo32 = new File("logo_32.png");
            if (!logo32.exists()) {
                FileIO.saveResource("logo_32.png", "logo_32.png");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean init() {
        DebugImpl.log("Loading Editor");
        Graphics.set(new OpenGL30());

        // Set GLFW Error callbacks to System console
        GLFWErrorCallback.createPrint(System.err).set();

        // Init GLFW and quit if error
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        // Set GLFW window hints and setup
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        // Create GLFW window
        glfwWindow = glfwCreateWindow(width, height, title, NULL, NULL);
        if (glfwWindow == NULL) throw new RuntimeException("Failed to create the window");

        // Create window icons
        try {
            final ByteBuffer[] list = new ByteBuffer[2];
            final IntBuffer width16 = BufferUtils.createIntBuffer(1);
            final IntBuffer height16 = BufferUtils.createIntBuffer(1);
            final IntBuffer channels16 = BufferUtils.createIntBuffer(1);
            list[0] = stbi_load("logo_16.png", width16, height16, channels16, 0);

            final IntBuffer width32 = BufferUtils.createIntBuffer(1);
            final IntBuffer height32 = BufferUtils.createIntBuffer(1);
            final IntBuffer channels32 = BufferUtils.createIntBuffer(1);
            list[1] = stbi_load("logo_32.png", width32, height32, channels32, 0);

            final GLFWImage.Buffer icons = GLFWImage.malloc(2);
            if (list[0] != null && list[1] != null) {
                icons.position(0).width(width16.get(0)).height(height16.get(0)).pixels(list[0]);
                icons.position(1).width(width32.get(0)).height(height32.get(0)).pixels(list[1]);
                icons.position(0);

                // Set window icons
                glfwSetWindowIcon(glfwWindow, icons);

                // Free up buffer space
                stbi_image_free(list[0]);
                stbi_image_free(list[1]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set window position from settings.
        glfwSetWindowPos(glfwWindow, winPosX, winPosY);

        // Make context and enable VSync
        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(1);
        glfwShowWindow(glfwWindow);

        // Set resize callbacks.
        // Input callbacks are set in ImGUI.
        try (GLFWWindowSizeCallback ignored = glfwSetWindowSizeCallback(glfwWindow, (window, width, height) -> {
            this.width = width;
            this.height = height;
        })) {
            Debug.log("Setting window size callbacks");
        } catch (Exception ignored) {}

        // Set position callbacks.
        try (GLFWWindowPosCallback ignored = glfwSetWindowPosCallback(glfwWindow, (window, x, y) -> {
            this.winPosX = x;
            this.winPosY = y;
        })) {
            Debug.log("Setting window position callbacks");
        } catch (Exception ignored) {}

        try (GLFWWindowFocusCallback ignored = glfwSetWindowFocusCallback(glfwWindow, (window, focused) -> {
            this.focused = focused;
            eventHandler.call(new WindowFocusEvent(focused));
        })) {
            Debug.log("Setting window focus callbacks");
        } catch (Exception ignored) {}

        // Create audio handler
        final String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);
        final int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice, attributes);
        alcMakeContextCurrent(audioContext);

        // Create OpenGL capabilities
        GL.createCapabilities();

        // Create OpenAL capabilities
        final ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        final ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
        if (!alCapabilities.OpenAL10) {
            Debug.logError("Audio AL10 library not supported!");
        } else {
            // Load audio decoders
            AudioDecoder.load();
        }

        // Enable blending
        Graphics.get().glEnable(GL_BLEND);
        Graphics.get().glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Set Java UI scheme
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and setup ImGUI
        imGui = new ImGUILayer(this);
        imGui.initImGui();

        // Setup and create framebuffer
        framebuffer = new Framebuffer[2];
        framebuffer[0] = new Framebuffer(this.width, this.height);
        framebuffer[1] = new Framebuffer(this.width, this.height);

        // Setup script manager
        if (EDITOR_MODE) {
            scriptManager = new EditorScriptManager();
            if (!((EditorScriptManager)scriptManager).canLoad()) {
                SystemIO.showPopupMessage("Error", "You are not running a JDK version! Currently running JRE " + SystemIO.getJavaVersion() + ". Exiting...", "Exit");
                return false;
            } else {
                listeners.add((EditorScriptManager) scriptManager);
            }
        } else {
            scriptManager = new RuntimeScriptManager();
        }

        // Load all built-in components
        if (EDITOR_MODE) {
            final Reflections reflections = new Reflections("caramel.api");
            final Set<Class<? extends Component>> set = reflections.getSubTypesOf(Component.class);
            final List<Class<? extends Component>> sorted = new ArrayList<>(set);
            sorted.sort(Comparator.comparing(Class::getName));
            for (final Class<? extends Component> c : sorted) {
                if (Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) continue;
                if (c == Transform.class || c == EditorCamera.class || c == UICamera.class) continue;
                Payload.COMPONENTS.add(c);
            }
        }

        // Register event listeners
        for (final Listener listener : listeners) {
            eventHandler.registerListener(listener);
        }

        // Load all the scripts
        scriptManager.reloadAll();

        return true;
    }

    private void loop() {
        Time.timeStarted = (float) glfwGetTime();
        float startTime = Time.timeStarted;
        float endTime;
        float second = 0;

        // Create the scene
        SceneImpl scene = null;
        final String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        final File source = new File(path);
        for (final String data : lastScenes) {
            SceneImpl loaded = null;
            if (EDITOR_MODE) {
                final File file = new File(data);
                loaded = loadScene(file);
            } else {
                try {
                    final String contents = FileIO.readUTFFromZip(source, data);
                    loaded = sceneLoader.loadScene(contents);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (scene == null && loaded != null) {
                scene = loaded;
            }
        }

        if (scene == null) {
            scene = sceneLoader.newScene();
        }

        DebugImpl.log("Opening scene " + scene.name);

        if (!EDITOR_MODE) {
            scene.play();
        }

        setTitle(scene.name);

        final Task task = scheduler.runTaskInterval(System::gc, 10000, 10000);
        float delta = 0;
        // Main loop
        while (!glfwWindowShouldClose(glfwWindow) && running) {
            if (Time.isSecond) {
                glfwSetWindowTitle(glfwWindow, (EDITOR_MODE ? "Caramel | " : "") + title + (getCurrentScene().isSaved() ? "" : "*") + " | FPS: " + (int) (Time.getFPS()));
            }

            if (process()) break;

            endTime = (float) glfwGetTime();
            delta = endTime - startTime;
            if (delta <= 0) continue;

            Time.deltaTime = delta;
            // Time.deltaTime = Math.max(Time.deltaTime, Time.minDeltaTime);
            startTime = endTime;

            second += Time.deltaTime;
            if (second >= 1f) {
                second = 0f;
                Time.isSecond = true;
            } else {
                Time.isSecond = false;
            }
        }

        for (final SceneImpl s : sceneLoader.getScenes()) {
            if (s.isPlaying()) {
                s.stop();
            }
        }

        task.runThenCancel();

        running = false;
    }

    public JsonObject getSettings() {
        return settings;
    }

    private boolean process() {
        final SceneImpl scene = getCurrentScene();
        if (scene == null) return true;

        glfwPollEvents();
        joystickListener.startFrame();

        if (EDITOR_MODE && ImGui.getIO().getKeyCtrl() && ImGui.isKeyPressed(Input.Key.P)) {
            if (scene.isPlaying()) scene.stop();
            else scene.play();
        }

        if (EDITOR_MODE) scene.editorUpdate();
        else scene.update();

        // Press F11 to maximize preview scene screen
        if (EDITOR_MODE && ImGui.isKeyPressed(Input.Key.F11)) {
            fullscreen = !fullscreen;
            eventHandler.call(new FullscreenEvent(fullscreen));
        }

        if (EDITOR_MODE) imGui.update();

        if (EDITOR_MODE && !fullscreen) {
            getSceneViewFramebuffer().bind();
            Graphics.get().glClearColor(0.4f, 0.4f, 0.4f, 0.5f);
            Graphics.get().glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            scene.render(scene.getEditorCamera());
            DebugDraw.INSTANCE.render(scene.getEditorCamera());
            getSceneViewFramebuffer().unbind();
        }
        BatchRenderer.DRAW_CALLS = 0;

        if (EDITOR_MODE && !fullscreen) getGameViewFramebuffer().bind();
        if (scene.getGameCameras().isEmpty()) {
            Graphics.get().glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            Graphics.get().glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        } else {
            Graphics.get().glClearColor(0.4f, 0.4f, 0.4f, 0.5f);
            Graphics.get().glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            for (final Camera camera : scene.getGameCameras()) {
                if (!camera.gameObject.active) continue;
                scene.render(camera);
            }
        }

        scene.render(scene.getUICamera());
        if (EDITOR_MODE && !fullscreen) getGameViewFramebuffer().unbind();

        if (fullscreen || !EDITOR_MODE) {
            mouseListener.setGameViewportPos(new Vector2f(0, 0));
            mouseListener.setGameViewportSize(new Vector2f(width, height));
        }

        if (EDITOR_MODE && !fullscreen) imGui.render();

        scene.endFrame();

        if (EDITOR_MODE && ImGui.getIO().getKeyCtrl() && ImGui.isKeyPressed(Input.Key.S)) {
            if (scene.isPlaying()) scene.stop();
            saveCurrentScene();
        }

        mouseListener.endFrame();
        keyListener.endFrame();
        joystickListener.endFrame();
        glfwSwapBuffers(glfwWindow);
        return false;
    }

    private void destroy() {
        // Save settings
        final File settings = new File("settings.json");
        this.settings = new JsonObject();
        if (EDITOR_MODE) {
            this.settings.addProperty("width", width);
            this.settings.addProperty("height", height);
            this.settings.addProperty("windowPosX", winPosX);
            this.settings.addProperty("windowPosY", winPosY);
            this.settings.addProperty("fullscreen", fullscreen);
            final JsonArray sceneData = new JsonArray();
            for (final SceneImpl scene : sceneLoader.getScenes()) {
                sceneData.add(FileIO.relativize(scene.getFile()));
            }
            this.settings.add("lastScene", sceneData);
        }

        // Destroy and clean up any remaining objects
        Texture.invalidateAll();
        Text.invalidateAll();
        MeshRenderer.invalidateAll();
        BatchRenderer.invalidateAll();
        Shader.invalidateAll();
        SoundSource.invalidateAll();
        sceneLoader.destroy();
        scriptManager.destroy();

        // Unregister any remaining listeners
        for (final Listener listener : listeners) {
            eventHandler.unregisterListener(listener);
        }

        // Destroy && saved ImGUI
        final String save = imGui.destroy();
        if (EDITOR_MODE) {
            this.settings.addProperty("imgui", save);
            FileIO.writeData(settings, serializer.toJson(this.settings));
        }

        // Free ALC context and close device
        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);

        // Free GLFW callbacks and destroy the window (if not yet destroyed)
        glfwFreeCallbacks(glfwWindow);
        try (GLFWErrorCallback errorCallback = glfwSetErrorCallback(null)) {
            if (errorCallback != null) {
                errorCallback.free();
            }
        } catch (Exception ignored) {}
        glfwDestroyWindow(glfwWindow);

        // Terminate the window
        glfwTerminate();

        System.exit(1);
    }

    public ImGUILayer getImGui() {
        return imGui;
    }

    public Framebuffer getSceneViewFramebuffer() {
        return framebuffer[0];
    }

    public Framebuffer getGameViewFramebuffer() {
        return framebuffer[1];
    }

    @Override
    public EventHandling getEventHandler() {
        return eventHandler;
    }

    @Override
    public void setTitle(final String title) {
        this.title = title;
        glfwSetWindowTitle(glfwWindow, (EDITOR_MODE ? "Caramel | " : "") + title + " | FPS: " + (int) (Time.getFPS()));
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public int getWinPosX() {
        return winPosX;
    }

    public int getWinPosY() {
        return winPosY;
    }

    @Override
    public SceneImpl getCurrentScene() {
        return sceneLoader.getCurrentScene();
    }

    @Override
    public SceneImpl loadScene(final File file) {
        return sceneLoader.loadScene(file);
    }

    @Override
    public SceneImpl loadScene(final int index) {
        return sceneLoader.loadScene(index);
    }

    @Override
    public void saveCurrentScene() {
        sceneLoader.saveCurrentScene();
    }

    @Override
    public void saveScene(final Scene scene, final File file) {
        sceneLoader.saveScene(scene, file);
    }

    @Override
    public void saveAllScenes() {
        sceneLoader.saveAllScenes();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setRunning(final boolean run) {
        this.running = run;
    }

    @Override
    public KeyListenerImpl getKeyListener() {
        return keyListener;
    }

    @Override
    public JoystickListener getJoystickListener() {
        return joystickListener;
    }

    @Override
    public MouseListenerImpl getMouseListener() {
        return mouseListener;
    }

    @Override
    public ScriptManager getScriptManager() {
        return scriptManager;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    public boolean isFocused() {
        return focused;
    }

    @Override
    public boolean isFullScreen() {
        return fullscreen;
    }

    public Gson getSerializer() {
        return serializer;
    }

    public SceneLoader getSceneLoader() {
        return sceneLoader;
    }
}
