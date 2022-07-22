package caramel.api.components;

import caramel.api.Application;
import caramel.api.Input;
import caramel.api.Time;
import caramel.api.interfaces.HideInEditor;
import caramel.api.objects.GameObject;
import imgui.ImGui;
import org.joml.Vector3f;
import xyz.destiall.caramel.app.editor.panels.HierarchyPanel;
import xyz.destiall.caramel.app.editor.panels.Panel;
import xyz.destiall.caramel.app.editor.panels.ScenePanel;

public final class EditorCamera extends Camera {
    @HideInEditor
    private static final float DELTA_ERROR = 0.0009f;
    private boolean perspective = true;
    public float sensitivity = 0.5f;

    public EditorCamera(GameObject gameObject) {
        super(gameObject);
        this.isEditor = true;
        //projection.perspective((float) Math.toRadians(fov), Application.getApp().getWidth() / (float) Application.getApp().getHeight(), near, far, new Matrix4f());
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {
        perspective = false;
        if (Panel.isWindowHovered(ScenePanel.class) && ImGui.isMouseDown(Input.Mouse.RIGHT)) {
            /**
            if (perspective) {
                if (Input.isKeyDown(Input.Key.W)) {
                    transform.position.add(target.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 5.f : 1), new Vector3f()));
                } else if (Input.isKeyDown(Input.Key.S)) {
                    transform.position.sub(target.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 5.f : 1), new Vector3f()));
                }
            }
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
            **/

            float mouseX = -Input.getMouseDeltaX() * sensitivity;
            float mouseY = Input.getMouseDeltaY() * sensitivity;

            transform.position.add(up.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 9f : 2f) * mouseY, new Vector3f()));
            Vector3f right = target.cross(up, new Vector3f());
            transform.position.add(right.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 9f : 2f) * mouseX));
        }

        GameObject selected = Application.getApp().getCurrentScene().getSelectedGameObject().stream().findFirst().orElse(null);

        if (Input.isKeyPressed(Input.Key.F) && selected != null &&
                (Panel.isWindowFocused(ScenePanel.class) || Panel.isWindowFocused(HierarchyPanel.class))) {
            gameObject.scene.getEditorCamera().transform.position.x = selected.transform.position.x;
            gameObject.scene.getEditorCamera().transform.position.y = selected.transform.position.y;
        }

        //if (Input.isKeyPressed(Input.Key.C)) {
        //    toggleCameraView();
        //}
    }

    private void toggleCameraView() {
        perspective = !perspective;
        // projection.identity();
    }

    @Override
    public void lateUpdate() {

    }

    //public Matrix4f getProjection() {
        //Matrix4f to;
        //if (perspective) {
        //    to = new Matrix4f().identity().perspective((float) Math.toRadians(fov), Application.getApp().getWidth() / (float) Application.getApp().getHeight(), near, far, new Matrix4f());
        //} else
        //{
            //projection = new Matrix4f().identity().ortho(-8, 8, -4.5f, 4.5f, near, far, true, new Matrix4f());

            //if (Math.abs(target.z) > DELTA_ERROR)
            //    target.lerp(new Vector3f(0, 0, -1), Time.deltaTime);
            //else {
            //    target.y = 0;
            //    target.x = 0;
            //    target.z = -1;
            //}

            //if (Math.abs(1 - up.y) > DELTA_ERROR) up.lerp(new Vector3f(up.x, 1f, up.z), Time.deltaTime);
            //else up.y = 1;
        //}

        //if (!projection.equals(to, DELTA_ERROR)) projection.lerp(to, Time.deltaTime);
        //else projection = to;

        //return projection;
    //}
}
