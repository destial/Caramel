package caramel.api.objects;

import caramel.api.Component;
import caramel.api.components.Transform;

import java.io.File;

/**
 * This represents a group of {@link Component}s in one object that is not linked to any scene.
 * This can also contain children {@link Prefab}s.
 */
public abstract class Prefab extends GameObject {
    protected final File file;

    public Prefab(final File file) {
        super(null);
        this.file = file;
    }

    /**
     * Get the file which this {@link Prefab} was loaded from.
     * @return The origin file.
     */
    public File getFile() {
        return file;
    }

    public GameObject instantiate(final Scene scene, final Transform parent) {
        final GameObject clone = this.clone(false);
        clone.setScene(scene);
        return super.instantiate(clone, parent);
    }

    public GameObject instantiate(final Scene scene) {
        final GameObject clone = this.clone(false);
        clone.setScene(scene);
        return super.instantiate(clone);
    }

    @Override
    public GameObject clone(final boolean copyId) {
        throw new UnsupportedOperationException("You cannot clone a prefab without a scene!");
    }

    @Override
    public GameObject instantiate(final GameObject prefab, final Transform parent) {
        throw new UnsupportedOperationException("You cannot instantiate a prefab without a scene!");
    }

    @Override
    public GameObject instantiate(final GameObject prefab) {
        throw new UnsupportedOperationException("You cannot instantiate a prefab without a scene!");
    }
}
