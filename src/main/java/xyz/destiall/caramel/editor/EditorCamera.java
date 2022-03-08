package xyz.destiall.caramel.editor;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.destiall.caramel.app.input.Input;
import xyz.destiall.caramel.app.Time;
import xyz.destiall.caramel.app.Application;
import xyz.destiall.caramel.components.Component;
import xyz.destiall.caramel.interfaces.HideInEditor;
import xyz.destiall.caramel.objects.GameObject;

import static org.lwjgl.glfw.GLFW.*;

public class EditorCamera extends Component {
    public Matrix4f projection, view, inverseProjection, inverseView;
    public Vector3f target;

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
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {
        if (Input.isMouseDown(GLFW_MOUSE_BUTTON_1)) {
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

            float mouseX = -Input.getMouseDeltaX();
            float mouseY = Input.getMouseDeltaY();

            target.rotateY(mouseX * Time.deltaTime);
            Vector3f right = up.cross(target, new Vector3f());
            target.rotateAxis(mouseY * Time.deltaTime, right.x, 0, right.z);
            forward.set(target.x, 0f, target.z).normalize();
            up = forward.cross(right.normalize(), new Vector3f());
            up.y = 1;
        }
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
        projection.identity();
        projection = projection.perspective((float) Math.toRadians(fov), Application.getApp().getWidth() / (float) Application.getApp().getHeight(), near, far);
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
