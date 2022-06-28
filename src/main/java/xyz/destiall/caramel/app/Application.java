package xyz.destiall.caramel.app;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.app.editor.Scene;
import xyz.destiall.caramel.app.editor.debug.DebugDraw;
import xyz.destiall.caramel.app.scripts.EditorScriptManager;
import xyz.destiall.caramel.app.serialize.SceneSerializer;
import xyz.destiall.caramel.app.ui.ImGUILayer;
import xyz.destiall.caramel.app.utils.FileIO;
import xyz.destiall.java.events.EventHandling;
import xyz.destiall.java.gson.Gson;
import xyz.destiall.java.gson.GsonBuilder;
import xyz.destiall.java.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
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
import static org.lwjgl.system.MemoryUtil.NULL;

public class Application implements Runnable {
    public final boolean EDITOR_MODE = true;
    private boolean running = false;

    private final MouseListener mouseListener;
    private final KeyListener keyListener;
    private final EventHandling eventHandling;
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

    private final List<Scene> scenes;
    private int sceneIndex = -1;

    private static Application a;

    public long glfwWindow;

    public static Application getApp() {
        if (a == null) a = new Application();
        return a;
    }

    private Application() {
        title = "Caramel";
        mouseListener = new MouseListener();
        keyListener = new KeyListener();
        eventHandling = new EventHandling();
        scenes = new ArrayList<>();
        serializer = new GsonBuilder()
                .registerTypeAdapter(Scene.class, new SceneSerializer())
                .serializeNulls()
                .setPrettyPrinting()
                .setLenient()
                .create();
    }

    public EventHandling getEventHandler() {
        return eventHandling;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Scene getCurrentScene() {
        return scenes.get(sceneIndex);
    }

    @Override
    public void run() {
        loadFiles();
        init();
        loop();
        destroy();
    }

    private void loadFiles() {
        try {
            File settings = new File("settings.json");
            JsonObject object = new JsonObject();
            if (settings.createNewFile()) {
                object.addProperty("width", width = 1280);
                object.addProperty("height", height = 720);
                object.addProperty("windowPosX", winPosX = 50);
                object.addProperty("windowPosY", winPosY = 50);
                object.addProperty("lastScene", lastScene = "assets/scenes/Untitled Scene.json");
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
            }
            File imgui = new File("imgui.ini");
            if (!imgui.exists()) {
                FileIO.saveResource("imgui.ini", "imgui.ini");
            }

            Thread.sleep(1000);
        } catch (IOException | InterruptedException e) {
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
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create GLFW window
        glfwWindow = glfwCreateWindow(width, height, title, NULL, NULL);
        if (glfwWindow == NULL) throw new RuntimeException("Failed to create the GLFW window");

        // Set resize callbacks.
        // Input callbacks are set in ImGUI
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

        // Set cursor mode
        //if (!EDITOR_MODE) {
        //    glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        //}

        // Make context and enable VSync
        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(1);
        glfwShowWindow(glfwWindow);

        // Some stuff with OpenGL
        GL.createCapabilities();
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        // Setup script manager
        scriptManager = new EditorScriptManager();

        // Register event listeners
        if (EDITOR_MODE) {
            eventHandling.registerListener(scriptManager);
        }

        // Create and setup ImGUI
        imGui = new ImGUILayer(this);
        imGui.initImGui();

        // Setup and create framebuffer
        framebuffer = new Framebuffer[2];
        framebuffer[0] = new Framebuffer(this.width, this.height);
        framebuffer[1] = new Framebuffer(this.width, this.height);
    }

    public Scene loadScene(File file) {
        Scene scene = file.exists() ? serializer.fromJson(FileIO.readData(file), Scene.class) : new Scene();
        if (!scenes.removeIf(s -> s.name.equals(scene.name))) {
            sceneIndex++;
        }
        scenes.add(scene);
        scene.setFile(file);
        return scene;
    }

    private void loop() {
        // Load all the scripts
        scriptManager.reloadAll();

        Time.timeStarted = System.currentTimeMillis();
        long startTime = Time.timeStarted;
        long endTime;
        float second = 0;

        // Create the scene
        File file = new File(lastScene);
        Scene scene = loadScene(file);

        running = true;

        if (!EDITOR_MODE) {
            scene.play();
        }

        setTitle(scene.name);

        // Main loop
        while (!glfwWindowShouldClose(glfwWindow) && running) {
            if (!EDITOR_MODE && Input.isKeyDown(GLFW_KEY_ESCAPE)) break;

            if (Time.isSecond) {
                glfwSetWindowTitle(glfwWindow, (EDITOR_MODE ? "Caramel | " : "") + title + " | FPS: " + (int) (Time.getFPS()));
            }

            if (process()) break;

            endTime = System.currentTimeMillis();
            Time.deltaTime = (endTime - startTime) / 1000f;
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
        Scene scene = getCurrentScene();
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

        if (scene.getGameCamera() != null) {
            if (EDITOR_MODE) getGameViewFramebuffer().bind();
            glClearColor(0.4f, 0.4f, 0.4f, 0.5f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            scene.render(scene.getGameCamera());
            if (EDITOR_MODE) getGameViewFramebuffer().unbind();
        }

        if (EDITOR_MODE) imGui.update();

        scene.endFrame();

        if (EDITOR_MODE && (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) || Input.isKeyDown(GLFW_KEY_RIGHT_CONTROL)) &&
                Input.isKeyPressed(GLFW_KEY_S)) {
            if (scene.isPlaying()) scene.stop();
            saveCurrentScene();
        }

        mouseListener.endFrame();
        keyListener.endFrame();

        glfwSwapBuffers(glfwWindow);
        return false;
    }

    public void saveCurrentScene() {
        Scene scene = getCurrentScene();
        saveScene(scene, scene.getFile());
    }

    public void saveScene(Scene scene, File file) {
        scene.setFile(file);
        String savedScene = serializer.toJson(scene);
        FileIO.writeData(file, savedScene);
        Debug.log("Saved scene " + scene.name);
    }

    public void saveAllScenes() {
        for (Scene scene : scenes) {
            saveScene(scene, scene.getFile());
        }
    }

    private void destroy() {
        // Destroy any remaining objects

        // Destroy and clear up any script class loaders
        scriptManager.destroy();

        // Unregister any remaining listeners
        eventHandling.unregisterListener(scriptManager);

        // Destroy ImGUI
        imGui.destroyImGui();

        // Save settings
        File settings = new File("settings.json");
        JsonObject object = new JsonObject();
        object.addProperty("width", width);
        object.addProperty("height", height);
        object.addProperty("windowPosX", winPosX);
        object.addProperty("windowPosY", winPosY);
        object.addProperty("lastScene", getCurrentScene().getFile().getPath());
        FileIO.writeData(settings, serializer.toJson(object));

        // Free GLFW callbacks and destroy the window (if not yet destroyed)
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate the window
        glfwTerminate();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean run) {
        this.running = run;
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

    public KeyListener getKeyListener() {
        return keyListener;
    }

    public MouseListener getMouseListener() {
        return mouseListener;
    }

    public EditorScriptManager getScriptManager() {
        return scriptManager;
    }
}
