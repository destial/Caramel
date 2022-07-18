package xyz.destiall.caramel.api.components;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Application;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.interfaces.HideInEditor;

public class Camera extends Component {
    public Matrix4f projection, view;
    public Vector3f target;

    @HideInEditor public boolean isEditor = false;
    @HideInEditor public Vector3f forward;
    @HideInEditor public Vector3f up;
    @HideInEditor public float fov = 60f;
    @HideInEditor public float near = 0.1f;
    @HideInEditor public float far = 1000f;

    public Camera(GameObject gameObject) {
        super(gameObject);
        projection = new Matrix4f();
        view = new Matrix4f();
        target = new Vector3f(transform.forward);
        forward = new Vector3f(target.x, 0f, target.z);
        up = new Vector3f(transform.up);

        projection.identity();
        //projection.perspective((float) Math.toRadians(fov), Application.getApp().getWidth() / (float) Application.getApp().getHeight(), near, far, new Matrix4f());
        //projection.ortho(-8, 8, -4.5f, 4.5f, near, far, true);
    }

    @Override
    public void start() {}

    @Override
    public void update() {
        double y = transform.rotation.z / Math.PI;
    }

    @Override
    public void editorUpdate() {}

    public Matrix4f getProjection() {
        float ratio = Application.getApp().getWidth() / (float) Application.getApp().getHeight();
        projection.identity();
        projection.ortho(-4.5f * ratio, 4.5f * ratio, -2.5f * ratio, 2.5f * ratio, near, far, true);
        return new Matrix4f(projection);
    }

    public Matrix4f getView() {
        view.identity();
        view.lookAt(transform.position, target.add(transform.position, new Vector3f()), up);
        return new Matrix4f(view);
    }

    public Matrix4f getInverseProjection() {
        return new Matrix4f(getProjection()).invert();
    }

    public Matrix4f getInverseView() {
        return new Matrix4f(getView()).invert();
    }
}
