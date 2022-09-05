package xyz.destiall.caramel.app.runtime;

import caramel.api.objects.Scene;
import xyz.destiall.caramel.app.ApplicationImpl;
import xyz.destiall.caramel.app.SceneLoader;

import java.io.File;

public final class RuntimeSceneLoader extends SceneLoader {
    public RuntimeSceneLoader(final ApplicationImpl application) {
        super(application);
    }

    @Override
    public void saveAllScenes() {}

    @Override
    public void saveCurrentScene() {}

    @Override
    public void saveScene(Scene scene, File file) {}
}
