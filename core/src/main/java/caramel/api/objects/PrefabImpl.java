package caramel.api.objects;

import java.io.File;

public final class PrefabImpl extends Prefab {

    public PrefabImpl(File file) {
        super(file);
    }

    public PrefabImpl(File file, GameObject gameObject) {
        super(file);
        this.name.set(gameObject.name.get());
        this.active = gameObject.active;
        this.components.addAll(gameObject.components);
        this.tags.addAll(gameObject.tags);
    }
}
