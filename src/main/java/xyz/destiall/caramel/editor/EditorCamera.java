package xyz.destiall.caramel.editor;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.interfaces.HideInEditor;
import xyz.destiall.caramel.api.GameObject;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

public class EditorCamera extends Component {
    public Matrix4f projection, view, inverseProjection, inverseView;
    public Vector3f target;
    private boolean perspective = true;

    @HideInEditor
    public Vector3f forward;

    @HideInEditor
    public Vector3f up;

    @HideInEditor
    public float fov = 60f;

    @HideInEditor
    public float near = 0.1f;

    @HideInEditor
    public float far = 1000f;

    public EditorCamera(GameObject gameObject) {
        super(gameObject);
        projection = new Matrix4f();
        view = new Matrix4f();
        target = new Vector3f(transform.forward);
        forward = new Vector3f(target.x, 0f, target.z);
        up = new Vector3f(transform.up);
        this.inverseProjection = new Matrix4f();
        this.inverseView = new Matrix4f();

        projection.identity();
        projection.perspective((float) Math.toRadians(fov), Application.getApp().getWidth() / (float) Application.getApp().getHeight(), near, far, new Matrix4f());
        inverseProjection = new Matrix4f(projection).invert();
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {
        // if (Input.isMouseDown(GLFW_MOUSE_BUTTON_1))
        {
            if (Input.isKeyDown(GLFW_KEY_W)) {
                transform.position.add(target.mul(Time.deltaTime * (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) ? 5.f : 1), new Vector3f()));
            } else if (Input.isKeyDown(GLFW_KEY_S)) {
                transform.position.sub(target.mul(Time.deltaTime * (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) ? 5.f : 1), new Vector3f()));
            }
            if (Input.isKeyDown(GLFW_KEY_A)) {
                Vector3f left = up.cross(target, new Vector3f());
                transform.position.add(left.mul(Time.deltaTime * (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) ? 5.f : 1)));
            } else if (Input.isKeyDown(GLFW_KEY_D)) {
                Vector3f right = target.cross(up, new Vector3f());
                transform.position.add(right.mul(Time.deltaTime * (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) ? 5.f : 1)));
            }

            if (Input.isKeyDown(GLFW_KEY_SPACE)) {
                transform.position.add(up.mul(Time.deltaTime * (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) ? 5.f : 1), new Vector3f()));
            } else if (Input.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
                transform.position.sub(up.mul(Time.deltaTime * (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) ? 5.f : 1), new Vector3f()));
            }

            // if (perspective)
            {
                float mouseX = -Input.getMouseDeltaX();
                float mouseY = Input.getMouseDeltaY();

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

        if (Input.isKeyPressed(GLFW_KEY_C)) {
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
        Vector3f pos = new Vector3f(transform.position);
        view = view.lookAt(pos, target.add(transform.position, new Vector3f()), up);
        inverseView = new Matrix4f(view).invert();
        return view;
    }

    public Matrix4f getProjection() {
        Matrix4f to;
        float delta = 0.0009f;
        if (perspective) {
            to = new Matrix4f().identity().perspective((float) Math.toRadians(fov), Application.getApp().getWidth() / (float) Application.getApp().getHeight(), near, far, new Matrix4f());
        } else {
            to = new Matrix4f().identity().ortho(-8, 8, -4.5f, 4.5f, near, far, true, new Matrix4f());
            if (Math.abs(target.y) > delta) target.lerp(new Vector3f(target.x, 0, target.z), Time.deltaTime);
            else target.y = 0;

            if (Math.abs(1 - up.y) > delta) up.lerp(new Vector3f(up.x, 1f, up.z), Time.deltaTime);
            else up.y = 1;
        }

        if (!projection.equals(to, delta)) projection.lerp(to, Time.deltaTime);
        else projection = to;

        inverseProjection = new Matrix4f(projection).invert();
        return projection;
    }

    public Matrix4f getInverseProjection() {
        return this.inverseProjection;
    }

    public Matrix4f getInverseView() {
        return this.inverseView;
    }

}
