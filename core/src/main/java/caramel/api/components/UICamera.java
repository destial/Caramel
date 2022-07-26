package caramel.api.components;

import caramel.api.objects.GameObject;
import caramel.api.render.Renderer;
import org.joml.Matrix4f;

public final class UICamera extends Camera {
    public UICamera(GameObject gameObject) {
        super(gameObject);
        this.isEditor = false;
        state = Renderer.State.UI;
    }

    @Override
    public void start() {}

    @Override
    public void update() {}

    @Override
    public void lateUpdate() {}

    @Override
    public Matrix4f getView() {
        view.identity();
        view.lookAlong(transform.forward, up);
        return new Matrix4f(view);
    }
}
