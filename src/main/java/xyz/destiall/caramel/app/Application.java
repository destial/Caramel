package xyz.destiall.caramel.app;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.app.editor.debug.DebugDraw;
import xyz.destiall.caramel.app.scripts.EditorScriptManager;
import xyz.destiall.caramel.app.serialize.SceneSerializer;
import xyz.destiall.caramel.app.ui.ImGUILayer;
import xyz.destiall.caramel.app.utils.FileIO;
import xyz.destiall.caramel.app.editor.Scene;
import xyz.destiall.java.events.EventHandling;
import xyz.destiall.java.gson.Gson;
import xyz.destiall.java.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
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

public class Application {
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
    private String title;

    private final List<Scene> scenes;
    private int sceneIndex = 0;

    private static Application a;

    public long glfwWindow;

    public static Application getApp() {
        if (a == null) a = new Application();
        return a;
    }

    private Application() {
        this.width = 1280;
        this.height = 720;
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

    public void run() {
        loadAssets();
        init();
        loop();
        destroy();
    }

    private void loadAssets() {
        try {
            File assets = new File("assets");
            if (assets.mkdir()) {
                saveResource("assets/shaders/color.glsl", false);
                saveResource("assets/shaders/default.glsl", false);
                saveResource("caramel_logo.png", false);

                wait(1000);
            }
        } catch (Exception e) {
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

        // Set cursor mode
        //if (!EDITOR_MODE) {
        //    glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        //}

        // Make context and enable VSync
        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(1);
        glfwShowWindow(glfwWindow);

        // Some janky stuff with OpenGL
        GL.createCapabilities();
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        // Setup script manager
        scriptManager = new EditorScriptManager();

        // Register event internals
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

    private void loop() {
        // Load all the scripts
        scriptManager.reloadAll();

        Time.timeStarted = System.currentTimeMillis();
        long startTime = Time.timeStarted;
        long endTime;
        float second = 0;

        // Create the scene
        File file = new File("assets/Untitled Scene.json");

        Scene scene = file.exists() ? serializer.fromJson(FileIO.readData(file), Scene.class) : new Scene();
        scenes.add(scene);

        running = true;

        if (!EDITOR_MODE) {
            scene.play();
            setTitle(scene.name);
        }

        // Main loop
        while (!glfwWindowShouldClose(glfwWindow) && running) {
            if (!EDITOR_MODE && Input.isKeyDown(GLFW_KEY_ESCAPE)) break;

            if (Time.isSecond) {
                glfwSetWindowTitle(glfwWindow, title + " | FPS: " + (int) (Time.getFPS()));
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

        if (scene.isPlaying()) scene.stop();

        if (EDITOR_MODE) {
            saveAllScenes();
        }

        running = false;
    }

    private boolean process() {
        glfwPollEvents();

        if (EDITOR_MODE) getCurrentScene().editorUpdate();
        else getCurrentScene().update();

        glViewport(0, 0, width, height);

        if (EDITOR_MODE) {
            getSceneViewFramebuffer().bind();
            glClearColor(0.4f, 0.4f, 0.4f, 0.5f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            getCurrentScene().render(getCurrentScene().getEditorCamera());
            DebugDraw.INSTANCE.render(getCurrentScene().getEditorCamera());
            getSceneViewFramebuffer().unbind();
        }

        if (getCurrentScene().getGameCamera() != null) {
            if (EDITOR_MODE) getGameViewFramebuffer().bind();
            glClearColor(0.4f, 0.4f, 0.4f, 0.5f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            getCurrentScene().render(getCurrentScene().getGameCamera());
            if (EDITOR_MODE) getGameViewFramebuffer().unbind();
        }

        if (EDITOR_MODE) imGui.update();

        getCurrentScene().endFrame();

        if (EDITOR_MODE && (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) || Input.isKeyDown(GLFW_KEY_RIGHT_CONTROL)) &&
                Input.isKeyPressed(GLFW_KEY_S)) {
            if (getCurrentScene().isPlaying()) getCurrentScene().stop();
            saveCurrentScene();
        }

        mouseListener.endFrame();
        keyListener.endFrame();

        glfwSwapBuffers(glfwWindow);
        return false;
    }

    public void saveCurrentScene() {
        String savedScene = serializer.toJson(getCurrentScene());
        FileIO.writeData(new File("assets/" + getCurrentScene().name + ".json"), savedScene);
        Debug.log("Saved scene " + getCurrentScene().name);
    }

    public void saveAllScenes() {
        for (Scene scene : scenes) {
            String savedScene = serializer.toJson(scene);
            FileIO.writeData(new File("assets/" + scene.name + ".json"), savedScene);
            Debug.log("Saved scene " + scene.name);
        }
    }

    private void destroy() {

        // Destroy any remaining objects
        scriptManager.destroy();

        // Unregister any remaining internals
        eventHandling.unregisterListener(scriptManager);

        // Destroy ImGUI
        imGui.destroyImGui();

        // Free GLFW callbacks and destroy the window (if not yet destroyed)
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate the window and free the error callbacks
        glfwTerminate();
        glfwSetErrorCallback(null).free();
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

    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found!");
            } else {
                File outFile = new File(resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(resourcePath.substring(0, Math.max(lastIndex, 0)));
                if (!outDir.exists()) outDir.mkdirs();

                try {
                    if (outFile.exists() && !replace) {
                        System.err.println("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
                    } else {
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];
                        int len;
                        while((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();
                    }
                } catch (IOException e) {
                    System.err.println("Could not save " + outFile.getName() + " to " + outFile);
                    e.printStackTrace();
                }

            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }

    public InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        } else {
            try {
                URL url = getClass().getClassLoader().getResource(filename);
                if (url == null) {
                    return null;
                } else {
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    return connection.getInputStream();
                }
            } catch (IOException e) {
                return null;
            }
        }
    }
}
