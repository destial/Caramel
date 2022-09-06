package caramel.api.objects;

import caramel.api.Component;
import caramel.api.components.Transform;
import caramel.api.interfaces.Render;
import caramel.api.interfaces.Update;

public final class GameObjectImpl extends GameObject implements Update, Render {

    public GameObjectImpl() {
        name = new StringWrapperImpl();
    }

    public GameObjectImpl(final Scene parentScene) {
        super(parentScene);
        name = new StringWrapperImpl("GameObject");
    }

    public GameObjectImpl clone(final boolean copyId) {
        GameObjectImpl clone;
        if (copyId) {
            clone = new GameObjectImpl();
            clone.scene = scene;
            clone.id = id;
            clone.transform = new Transform(clone);
            clone.transform.id = transform.id;
        } else {
            clone = new GameObjectImpl(scene);
        }
        clone.name.set(name.get());
        clone.transform.position.set(transform.position);
        clone.transform.localPosition.set(transform.localPosition);
        clone.transform.rotation.set(transform.rotation);
        clone.transform.scale.set(transform.scale);
        clone.transform.forward.set(transform.forward);
        clone.transform.enabled = transform.enabled;
        clone.active = active;
        for (final Component c : components) {
            if (c instanceof Transform) continue;
            Component cl = c.clone(clone, copyId);
            clone.addComponent(cl);
        }
        for (final GameObject c : children) {
            GameObject ch = c.clone(copyId);
            ch.parent = clone.transform;
            clone.children.add(ch);
        }
        return clone;
    }

    public GameObjectImpl instantiate(final GameObjectImpl prefab, final Transform parent) {
        final GameObjectImpl clone = prefab.clone(false);
        if (parent != null) {
            scene.addGameObject(clone, parent.gameObject);
        }
        return clone;
    }

    public GameObjectImpl instantiate(final GameObjectImpl prefab) {
        return instantiate(prefab, null);
    }
}
