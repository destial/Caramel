package caramel.api.components;

import caramel.api.Application;
import caramel.api.Component;
import caramel.api.interfaces.HideInEditor;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import caramel.api.objects.GameObject;

public class Camera extends Component {
    public transient Matrix4f projection, view;
    public Vector3f target;
    public boolean rotate = false;

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
    }

    @Override
    public void start() {}

    @Override
    public void update() {}

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
        if (rotate) {
            double x = -Math.sin(transform.rotation.z);
            double y = Math.cos(transform.rotation.z);
            up.set(x, y, 0);
        } else {
            up.set(0, 1, 0);
        }
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
