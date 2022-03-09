package xyz.destiall.caramel.app;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import xyz.destiall.caramel.app.input.Input;
import xyz.destiall.caramel.app.scripts.ScriptManager;
import xyz.destiall.caramel.app.ui.ImGUILayer;
import xyz.destiall.caramel.editor.Scene;
import xyz.destiall.caramel.editor.Time;
import xyz.destiall.caramel.graphics.Framebuffer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Application {
    private final boolean editorMode = true;

    private final MouseListener mouseListener;
    private final KeyListener keyListener;
    private ImGUILayer imGui;
    private Framebuffer framebuffer;
    private ScriptManager scriptManager;

    private int width;
    private int height;
    private String title;
    private Scene scene;

    private static Application a;

    public long glfwWindow;

    private Application() {
        this.width = 1280;
        this.height = 720;
        title = "Caramel";
        mouseListener = new MouseListener();
        keyListener = new KeyListener();
    }

    public static Application getApp() {
        if (a == null) a = new Application();
        return a;
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
        return scene;
    }

    public void run() {
        try {
            File assets = new File("assets");
            if (assets.mkdir()) {
                saveResource("/assets/shaders/color.glsl", false);
                saveResource("/assets/shaders/default.glsl", false);
                saveResource("caramel_logo.png", false);

                wait(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        init();
        loop();

        imGui.destroyImGui();

        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindow = glfwCreateWindow(width, height, title, NULL, NULL);
        if (glfwWindow == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(glfwWindow, keyListener::keyCallback);
        glfwSetCursorPosCallback(glfwWindow, mouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, mouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, mouseListener::mouseScrollCallback);

        glfwSetWindowSizeCallback(glfwWindow, (window, width, height) -> {
            this.width = width;
            this.height = height;
            //glViewport(0, 0, width, height);
            //framebuffer.resize(width, height);

            // glViewport(0, 0, width, height);
        });

        if (!editorMode) {
            glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(1);
        glfwShowWindow(glfwWindow);

        GL.createCapabilities();
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        imGui = new ImGUILayer(this);
        imGui.initImGui();

        framebuffer = new Framebuffer(this.width, this.height);

        scriptManager = new ScriptManager();
    }

    private void loop() {
        float startTime = Time.getElapsedTime();
        float endTime;
        float second = 0;

        scene = new Scene();
        scene.init();

        scriptManager.reloadAll();

        while (!glfwWindowShouldClose(glfwWindow)) {
            if (Input.isKeyDown(GLFW_KEY_ESCAPE)) break;
            if (Time.isSecond) {
                glfwSetWindowTitle(glfwWindow, title + " | FPS: " + (int) (Time.getFPS()));
            }

            glfwPollEvents();

            glViewport(0, 0, width, height);
            framebuffer.bind();

            glClearColor(0.4f, 0.4f, 0.4f, 0.5f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (!editorMode) scene.update();
            else scene.editorUpdate();

            framebuffer.unbind();
            if (editorMode) imGui.update();

            scene.endFrame();
            mouseListener.endFrame();
            keyListener.endFrame();

            glfwSwapBuffers(glfwWindow);
            endTime = Time.getElapsedTime();
            Time.deltaTime = endTime - startTime;
            if (Time.deltaTime <= 0) {
                Time.deltaTime = 1 / 60f;
            }
            startTime = endTime;

            second += Time.deltaTime;
            if (second >= 1f) {
                second = 0f;
                Time.isSecond = true;
            } else {
                Time.isSecond = false;
            }
        }
    }

    public ImGUILayer getImGui() {
        return imGui;
    }

    public Framebuffer getFramebuffer() {
        return framebuffer;
    }

    public KeyListener getKeyListener() {
        return keyListener;
    }

    public MouseListener getMouseListener() {
        return mouseListener;
    }

    public ScriptManager getScriptManager() {
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
                } catch (IOException ex) {
                    System.err.println("Could not save " + outFile.getName() + " to " + outFile);
                    ex.printStackTrace();
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
            } catch (IOException var4) {
                return null;
            }
        }
    }
}
