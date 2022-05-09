package xyz.destiall.caramel.api.components;

import xyz.destiall.caramel.editor.EditorCamera;
import xyz.destiall.caramel.api.GameObject;

public class Camera extends EditorCamera {
    public Camera(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {}

    @Override
    public void update() {}

    @Override
    public void editorUpdate() {}
}
