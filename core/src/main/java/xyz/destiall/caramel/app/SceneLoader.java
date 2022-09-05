package xyz.destiall.caramel.app;

import caramel.api.debug.DebugImpl;
import caramel.api.objects.GameObject;
import caramel.api.objects.GameObjectImpl;
import caramel.api.objects.Scene;
import caramel.api.objects.SceneImpl;
import caramel.api.render.MeshRenderer;
import caramel.api.texture.mesh.QuadMesh;
import caramel.api.utils.FileIO;
import xyz.destiall.java.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class SceneLoader {
    private final List<SceneImpl> scenes;
    private int sceneIndex = -1;
    private final Gson serializer;

    public SceneLoader(final ApplicationImpl application) {
        scenes = new ArrayList<>();
        serializer = application.getSerializer();
    }

    public SceneImpl loadScene(final String data) {
        final SceneImpl scene = serializer.fromJson(data, SceneImpl.class);
        if (scene == null) return null;
        if (!scenes.removeIf(s -> s.name.equals(scene.name))) {
            sceneIndex++;
        }
        scenes.add(scene);
        return scene;
    }

    public SceneImpl loadScene(File file) {
        SceneImpl scene = scenes.stream().filter(s -> s.getFile().equals(file)).findFirst().orElse(null);
        if (scene != null) {
            sceneIndex = scenes.indexOf(scene);
            return scene;
        }
        scene = loadScene(FileIO.readData(file));
        scene.setFile(file);
        return scene;
    }

    public SceneImpl loadScene(final int index) {
        if (index < 0 || index >= scenes.size()) {
            return getCurrentScene();
        }
        sceneIndex = index;
        return getCurrentScene();
    }

    public List<SceneImpl> getScenes() {
        return scenes;
    }

    public void saveCurrentScene() {
        final SceneImpl scene = getCurrentScene();
        saveScene(scene, scene.getFile());
    }

    public void saveScene(final Scene scene, final File file) {
        scene.setFile(file);
        final String savedScene = serializer.toJson(scene);
        if (FileIO.writeData(file, savedScene)) {
            ((SceneImpl) scene).setSaved(true);
            DebugImpl.log("Saved scene " + scene.name);
        } else {
            DebugImpl.logError("Unable to save scene " + scene.name);
        }
    }

    public void saveAllScenes() {
        for (final SceneImpl scene : scenes) {
            saveScene(scene, scene.getFile());
        }
    }

    public SceneImpl getCurrentScene() {
        if (scenes.isEmpty()) return null;
        return scenes.get(sceneIndex);
    }

    public SceneImpl newScene() {
        final SceneImpl scene = new SceneImpl();
        final GameObject gameObject = new GameObjectImpl(scene);
        final MeshRenderer meshRenderer = new MeshRenderer(gameObject);
        meshRenderer.setMesh(new QuadMesh(1));
        meshRenderer.build();
        gameObject.addComponent(meshRenderer);
        scene.addGameObject(gameObject);
        scenes.add(scene);
        sceneIndex++;
        return scene;
    }

    public void destroy() {
        for (final SceneImpl scene : scenes) {
            scene.invalidate();
        }
        scenes.clear();
    }
}
