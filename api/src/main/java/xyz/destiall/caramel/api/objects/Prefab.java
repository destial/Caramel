package xyz.destiall.caramel.api.objects;

import xyz.destiall.caramel.api.components.Transform;

import java.io.File;

public abstract class Prefab extends GameObject {
    protected final File file;

    public Prefab(File file) {
        super(null);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public GameObject instantiate(Scene scene, Transform parent) {
        GameObject clone = this.clone(false);
        clone.setScene(scene);
        return super.instantiate(clone, parent);
    }

    public GameObject instantiate(Scene scene) {
        GameObject clone = this.clone(false);
        clone.setScene(scene);
        return super.instantiate(clone);
    }

    @Override
    public GameObject clone(boolean copyId) {
        throw new UnsupportedOperationException("You cannot clone a prefab without a scene!");
    }

    @Override
    public GameObject instantiate(GameObject prefab, Transform parent) {
        throw new UnsupportedOperationException("You cannot instantiate a prefab without a scene!");
    }

    @Override
    public GameObject instantiate(GameObject prefab) {
        throw new UnsupportedOperationException("You cannot instantiate a prefab without a scene!");
    }
}
