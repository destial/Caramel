package xyz.destiall.caramel.app;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;
import org.reflections.Reflections;
import caramel.api.Application;
import caramel.api.Component;
import caramel.api.Input;
import caramel.api.Time;
import caramel.api.audio.AudioListener;
import caramel.api.components.Transform;
import caramel.api.debug.Debug;
import caramel.api.debug.DebugImpl;
import caramel.api.objects.Scene;
import caramel.api.utils.FileIO;
import caramel.api.objects.SceneImpl;
import xyz.destiall.caramel.app.editor.debug.DebugDraw;
import xyz.destiall.caramel.app.scripts.EditorScriptManager;
import xyz.destiall.caramel.app.serialize.SceneSerializer;
import xyz.destiall.caramel.app.ui.ImGUILayer;
import xyz.destiall.caramel.app.utils.Payload;
import xyz.destiall.java.events.EventHandling;
import xyz.destiall.java.events.Listener;
import xyz.destiall.java.gson.Gson;
import xyz.destiall.java.gson.GsonBuilder;
import xyz.destiall.java.gson.JsonObject;
import xyz.destiall.java.timer.Scheduler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
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
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class ApplicationImpl extends Application {
    public boolean EDITOR_MODE = true;
    private boolean running = false;

    private final MouseListenerImpl mouseListener;
    private final KeyListenerImpl keyListener;
    private final EventHandling eventHandler;
    private final Scheduler scheduler;
    private final List<Listener> listeners;
    private final Gson serializer;

    private ImGUILayer imGui;
    private Framebuffer[] framebuffer;
    private EditorScriptManager scriptManager;

    private int width;
    private int height;
    private int winPosX;
    private int winPosY;
    private String lastScene;
    private String title;

    private long audioContext;
    private long audioDevice;

    private final List<SceneImpl> scenes;
    private int sceneIndex = -1;

    public long glfwWindow;

    public static ApplicationImpl getApp() {
        if (inst == null) inst = new ApplicationImpl();
        return (ApplicationImpl) inst;
    }

    private ApplicationImpl() {
        title = "Caramel";
        mouseListener = new MouseListenerImpl();
        keyListener = new KeyListenerImpl();
        eventHandler = new EventHandling();
        scheduler = new Scheduler();
        scenes = new ArrayList<>();
        serializer = new GsonBuilder()
                .registerTypeAdapter(SceneImpl.class, new SceneSerializer())
                .serializeNulls()
                .setPrettyPrinting()
                .setLenient()
                .create();
        listeners = new ArrayList<>();
        listeners.add(new AudioListener());
    }

    @Override
    public void run() {
        setup();
        init();
        loop();
        destroy();
    }

    @SuppressWarnings("all")
    private void setup() {
        // Load settings and data files
        try {
            File settings = new File("settings.json");
            JsonObject object = new JsonObject();
            if (settings.createNewFile()) {
                object.addProperty("width", width = 1920);
                object.addProperty("height", height = 1080);
                object.addProperty("windowPosX", winPosX = 50);
                object.addProperty("windowPosY", winPosY = 50);
                object.addProperty("lastScene", lastScene = "assets/scenes/Untitled Scene.caramel");
                FileIO.writeData(settings, serializer.toJson(object));

            } else {
                object = serializer.fromJson(FileIO.readData(settings), JsonObject.class);
                width = object.get("width").getAsInt();
                height = object.get("height").getAsInt();
                winPosX = object.get("windowPosX").getAsInt();
                winPosY = object.get("windowPosY").getAsInt();
                lastScene = object.get("lastScene").getAsString();
            }

            File assets = new File("assets" + File.separator);
            if (!assets.exists()) {
                assets.mkdirs();
                new File(assets, "models" + File.separator).mkdirs();
                new File(assets, "textures" + File.separator).mkdirs();
                new File(assets, "shaders" + File.separator).mkdirs();
                new File(assets, "scenes" + File.separator).mkdirs();
                new File(assets, "sounds" + File.separator).mkdirs();
                new File(assets, "fonts" + File.separator).mkdirs();
                FileIO.saveResource("arial.TTF", "assets/fonts/arial.TTF");
            }

            File imgui = new File("imgui.ini");
            if (!imgui.exists()) {
                FileIO.saveResource("imgui.ini", "imgui.ini");
            }

            File logo16 = new File("logo_16.png");
            if (!logo16.exists()) {
                FileIO.saveResource("logo_16.png", "logo_16.png");
            }

            File logo32 = new File("logo_32.png");
            if (!logo32.exists()) {
                FileIO.saveResource("logo_32.png", "logo_32.png");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        // Set GLFW Error callbacks to System console
        GLFWErrorCallback.createPrint(System.err).set();

        // Init GLFW and quit if error
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        // Set GLFW window hints and setup
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        // Create GLFW window
        glfwWindow = glfwCreateWindow(width, height, title, NULL, NULL);
        if (glfwWindow == NULL) throw new RuntimeException("Failed to create the GLFW window");

        // Create window icons
        try {
            ByteBuffer[] list = new ByteBuffer[2];
            IntBuffer width16 = BufferUtils.createIntBuffer(1);
            IntBuffer height16 = BufferUtils.createIntBuffer(1);
            IntBuffer channels16 = BufferUtils.createIntBuffer(1);
            list[0] = stbi_load("logo_16.png", width16, height16, channels16, 0);

            IntBuffer width32 = BufferUtils.createIntBuffer(1);
            IntBuffer height32 = BufferUtils.createIntBuffer(1);
            IntBuffer channels32 = BufferUtils.createIntBuffer(1);
            list[1] = stbi_load("logo_32.png", width32, height32, channels32, 0);

            GLFWImage.Buffer icons = GLFWImage.malloc(2);
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

        // Set resize callbacks.
        // Input callbacks are set in ImGUI.
        glfwSetWindowSizeCallback(glfwWindow, (window, width, height) -> {
            this.width = width;
            this.height = height;
        });

        // Set position callbacks.
        glfwSetWindowPosCallback(glfwWindow, (window, x, y) -> {
            this.winPosX = x;
            this.winPosY = y;
        });

        // Set window position from settings.
        glfwSetWindowPos(glfwWindow, winPosX, winPosY);

        // Make context and enable VSync
        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(1);
        glfwShowWindow(glfwWindow);

        // Create audio handler
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice, attributes);
        alcMakeContextCurrent(audioContext);

        // Create OpenGL capabilities
        GL.createCapabilities();

        // Create OpenAL capabilities
        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
        if (!alCapabilities.OpenAL10) {
            Debug.logError("Audio AL10 library not supported!");
        }

        // Enable blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        // Setup script manager
        scriptManager = new EditorScriptManager();

        // Register event listeners
        listeners.add(scriptManager);
        for (Listener listener : listeners) {
            eventHandler.registerListener(listener);
        }

        // Create and setup ImGUI
        imGui = new ImGUILayer(this);
        imGui.initImGui();

        // Setup and create framebuffer
        framebuffer = new Framebuffer[2];
        framebuffer[0] = new Framebuffer(this.width, this.height);
        framebuffer[1] = new Framebuffer(this.width, this.height);

        // Load all the scripts
        scriptManager.reloadAll();

        // Load all built-in components
        Reflections reflections = new Reflections("caramel.api");
        Set<Class<? extends Component>> set = reflections.getSubTypesOf(Component.class);
        List<Class<? extends Component>> sorted = new ArrayList<>(set);
        sorted.sort(Comparator.comparing(Class::getName));
        for (Class<? extends Component> c : sorted) {
            if (Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) continue;
            if (c == Transform.class) continue;
            Payload.COMPONENTS.add(c);
        }
    }

    private void loop() {
        Time.timeStarted = (float) glfwGetTime();
        float startTime = Time.timeStarted;
        float endTime;
        float second = 0;

        // Create the scene
        File file = new File(lastScene);
        SceneImpl scene = loadScene(file);

        DebugImpl.log("Opening scene " + scene.name);

        running = true;

        if (!EDITOR_MODE) {
            scene.play();
        }

        setTitle(scene.name);

        // Main loop
        while (!glfwWindowShouldClose(glfwWindow) && running) {
            if (!EDITOR_MODE && Input.isKeyDown(Input.Key.ESCAPE)) break;

            if (Time.isSecond) {
                glfwSetWindowTitle(glfwWindow, (EDITOR_MODE ? "Caramel | " : "") + title + (getCurrentScene().isSaved() ? "" : "*") + " | FPS: " + (int) (Time.getFPS()));
            }

            if (process()) break;

            endTime = (float) glfwGetTime();
            Time.deltaTime = endTime - startTime;
            Time.deltaTime = Math.max(Time.deltaTime, Time.minDeltaTime);
            startTime = endTime;

            second += Time.deltaTime;
            if (second >= 1f) {
                second = 0f;
                Time.isSecond = true;
            } else {
                Time.isSecond = false;
            }
        }

        if (getCurrentScene().isPlaying()) getCurrentScene().stop();

        if (EDITOR_MODE) {
            saveAllScenes();
        }

        running = false;
    }

    private boolean process() {
        SceneImpl scene = getCurrentScene();
        glfwPollEvents();

        if (EDITOR_MODE) scene.editorUpdate();
        else scene.update();

        glViewport(0, 0, width, height);

        if (EDITOR_MODE) {
            getSceneViewFramebuffer().bind();
            glClearColor(0.4f, 0.4f, 0.4f, 0.5f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            scene.render(scene.getEditorCamera());
            DebugDraw.INSTANCE.render(scene.getEditorCamera());
            getSceneViewFramebuffer().unbind();
        }

        if (EDITOR_MODE) getGameViewFramebuffer().bind();
        if (scene.getGameCamera() == null || !scene.getGameCamera().gameObject.active) {
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        } else {
            glClearColor(0.4f, 0.4f, 0.4f, 0.5f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            scene.render(scene.getGameCamera());
        }
        if (EDITOR_MODE) getGameViewFramebuffer().unbind();

        if (EDITOR_MODE) imGui.update();

        scene.endFrame();

        if (EDITOR_MODE && Input.isControlPressedAnd(Input.Key.S)) {
            if (scene.isPlaying()) scene.stop();
            saveCurrentScene();
        }

        mouseListener.endFrame();
        keyListener.endFrame();

        glfwSwapBuffers(glfwWindow);
        return false;
    }

    private void destroy() {
        // Save settings
        File settings = new File("settings.json");
        JsonObject object = new JsonObject();
        object.addProperty("width", width);
        object.addProperty("height", height);
        object.addProperty("windowPosX", winPosX);
        object.addProperty("windowPosY", winPosY);
        object.addProperty("lastScene", getCurrentScene().getFile().getPath());
        FileIO.writeData(settings, serializer.toJson(object));

        // Destroy and clean up any remaining objects
        scriptManager.destroy();

        // Unregister any remaining listeners
        for (Listener listener : listeners) {
            eventHandler.unregisterListener(listener);
        }

        // Destroy ImGUI
        imGui.destroyImGui();

        // Free ALC context and close device
        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);

        // Free GLFW callbacks and destroy the window (if not yet destroyed)
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate the window
        glfwTerminate();
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
    public void setTitle(String title) {
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

    @Override
    public SceneImpl getCurrentScene() {
        return scenes.get(sceneIndex);
    }

    @Override
    public SceneImpl loadScene(File file) {
        SceneImpl scene = scenes.stream().filter(s -> s.getFile().equals(file)).findFirst().orElse(null);
        if (scene != null) {
            sceneIndex = scenes.indexOf(scene);
            return scene;
        }
        scene = file.exists() ? serializer.fromJson(FileIO.readData(file), SceneImpl.class) : new SceneImpl();

        SceneImpl finalScene = scene;
        if (!scenes.removeIf(s -> s.name.equals(finalScene.name))) {
            sceneIndex++;
        }
        scenes.add(scene);
        scene.setFile(file);
        return scene;
    }

    @Override
    public SceneImpl loadScene(int index) {
        sceneIndex = index;
        return getCurrentScene();
    }

    @Override
    public void saveCurrentScene() {
        SceneImpl scene = getCurrentScene();
        saveScene(scene, scene.getFile());
    }

    @Override
    public void saveScene(Scene scene, File file) {
        scene.setFile(file);
        String savedScene = serializer.toJson(scene);
        if (FileIO.writeData(file, savedScene)) {
            ((SceneImpl) scene).setSaved(true);
            DebugImpl.log("Saved scene " + scene.name);
        } else {
            DebugImpl.log("Unable to save scene " + scene.name);
        }
    }

    @Override
    public void saveAllScenes() {
        for (SceneImpl scene : scenes) {
            saveScene(scene, scene.getFile());
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setRunning(boolean run) {
        this.running = run;
    }

    @Override
    public KeyListenerImpl getKeyListener() {
        return keyListener;
    }

    @Override
    public MouseListenerImpl getMouseListener() {
        return mouseListener;
    }

    @Override
    public EditorScriptManager getScriptManager() {
        return scriptManager;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }
}
