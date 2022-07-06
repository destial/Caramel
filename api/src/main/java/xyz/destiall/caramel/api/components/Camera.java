package xyz.destiall.caramel.api.components;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Application;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.interfaces.HideInEditor;

public class Camera extends Component {
    public transient Matrix4f projection, view;
    public transient Vector3f target;

    @HideInEditor public boolean isEditor = false;
    @HideInEditor public Vector3f forward;
    @HideInEditor public Vector3f up;
    public transient float near = 0.1f;
    public transient float far = 1000f;
    public transient float zoom = 4.5f;

    public Camera(GameObject gameObject) {
        super(gameObject);
        projection = new Matrix4f();
        view = new Matrix4f();
        transform.position.set(transform.position.x, transform.position.y, 1f);
        target = new Vector3f(transform.forward);
        forward = new Vector3f(target.x, 0f, target.z);
        up = new Vector3f(transform.up);

        projection.identity();
    }

    @Override
    public void start() {}

    public Matrix4f getProjection() {
        float ratio = Application.getApp().getWidth() / (float) Application.getApp().getHeight();
        projection.identity();
        projection.ortho(-ratio * zoom, ratio * zoom, -zoom * 0.5f, zoom * 0.5f, near, far, true);
        return projection;
    }

    public Matrix4f getView() {
        view.identity();
        view.lookAt(transform.position, target.add(transform.position, new Vector3f()), up);
        return view;
    }

    public Matrix4f getInverseProjection() {
        return new Matrix4f(getProjection()).invert();
    }

    public Matrix4f getInverseView() {
        return new Matrix4f(getView()).invert();
    }
}
