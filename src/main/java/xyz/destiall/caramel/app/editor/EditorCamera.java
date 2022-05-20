package xyz.destiall.caramel.app.editor;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.interfaces.HideInEditor;

public class EditorCamera extends Component {
    @HideInEditor private transient static final float DELTA_ERROR = 0.0009f;

    public Matrix4f projection, view;
    public Vector3f target;
    private boolean perspective = true;

    @HideInEditor public Vector3f forward;
    @HideInEditor public Vector3f up;
    @HideInEditor public float fov = 60f;
    @HideInEditor public float near = 0.1f;
    @HideInEditor public float far = 1000f;

    public float sensitivity = 0.5f;

    public EditorCamera(GameObject gameObject) {
        super(gameObject);
        projection = new Matrix4f();
        view = new Matrix4f();
        target = new Vector3f(transform.forward);
        forward = new Vector3f(target.x, 0f, target.z);
        up = new Vector3f(transform.up);

        projection.identity();
        projection.perspective((float) Math.toRadians(fov), Application.getApp().getWidth() / (float) Application.getApp().getHeight(), near, far, new Matrix4f());
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {
        if (Input.isMouseDown(Input.Mouse.LEFT)) {
            //if (perspective) {
                if (Input.isKeyDown(Input.Key.W)) {
                    transform.position.add(target.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 5.f : 1), new Vector3f()));
                } else if (Input.isKeyDown(Input.Key.S)) {
                    transform.position.sub(target.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 5.f : 1), new Vector3f()));
                }
            //}
            if (Input.isKeyDown(Input.Key.A)) {
                Vector3f left = up.cross(target, new Vector3f());
                transform.position.add(left.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 5.f : 1)));
            } else if (Input.isKeyDown(Input.Key.D)) {
                Vector3f right = target.cross(up, new Vector3f());
                transform.position.add(right.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 5.f : 1)));
            }

            if (Input.isKeyDown(Input.Key.SPACE)) {
                transform.position.add(up.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 5.f : 1), new Vector3f()));
            } else if (Input.isKeyDown(Input.Key.L_SHIFT)) {
                transform.position.sub(up.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 5.f : 1), new Vector3f()));
            }

            if (perspective && Input.isMouseDown(Input.Mouse.LEFT)) {
                float mouseX = -Input.getMouseDeltaX() * sensitivity;
                float mouseY = Input.getMouseDeltaY() * sensitivity;

                target.rotateY(mouseX * Time.deltaTime);
                Vector3f right = up.cross(target, new Vector3f());
                right.y = 0;
                right.normalize();
                target.rotateAxis(mouseY * Time.deltaTime, right.x, 0, right.z);

                forward.set(target.x, 0f, target.z).normalize();
                up = forward.cross(right.normalize(), new Vector3f());
                up.y = 1;
                up.normalize();
            }
        }

        if (Input.isKeyPressed(Input.Key.C)) {
            toggleCameraView();
        }
    }

    public void toggleCameraView() {
        perspective = !perspective;
        // projection.identity();
    }

    @Override
    public void lateUpdate() {

    }

    public Matrix4f getView() {
        view.identity();
        view.lookAt(transform.position, target.add(transform.position, new Vector3f()), up);
        return view;
    }

    public Matrix4f getProjection() {
        Matrix4f to;

        if (perspective) {
            to = new Matrix4f().identity().perspective((float) Math.toRadians(fov), Application.getApp().getWidth() / (float) Application.getApp().getHeight(), near, far, new Matrix4f());
        } else {
            to = new Matrix4f().identity().ortho(-8, 8, -4.5f, 4.5f, near, far, true, new Matrix4f());
            if (Math.abs(target.z) > DELTA_ERROR)
                target.lerp(new Vector3f(0, 0, -1), Time.deltaTime);
            else {
                target.y = 0;
                target.x = 0;
                target.z = -1;
            }

            if (Math.abs(1 - up.y) > DELTA_ERROR) up.lerp(new Vector3f(up.x, 1f, up.z), Time.deltaTime);
            else up.y = 1;
        }

        if (!projection.equals(to, DELTA_ERROR)) projection.lerp(to, Time.deltaTime);
        else projection = to;

        return projection;
    }

    public Matrix4f getInverseProjection() {
        return new Matrix4f(projection).invert();
    }

    public Matrix4f getInverseView() {
        return new Matrix4f(view).invert();
    }

}
