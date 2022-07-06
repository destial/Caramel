package xyz.destiall.caramel.api.components;

import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Application;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.app.editor.ui.ScenePanel;
import xyz.destiall.caramel.app.editor.ui.Panel;
import xyz.destiall.caramel.api.interfaces.HideInEditor;

public final class EditorCamera extends Camera {
    @HideInEditor private static final float DELTA_ERROR = 0.0009f;
    public transient float sensitivity = 0.5f;

    public EditorCamera(GameObject gameObject) {
        super(gameObject);
        this.isEditor = true;
        //projection.perspective((float) Math.toRadians(fov), Application.getApp().getWidth() / (float) Application.getApp().getHeight(), near, far, new Matrix4f());
    }

    @Override
    public void update() {
        if (Panel.isWindowFocused(ScenePanel.class) && ImGui.isMouseDown(Input.Mouse.RIGHT)) {

            float mouseX = -Input.getMouseDeltaX() * sensitivity;
            float mouseY = Input.getMouseDeltaY() * sensitivity;

            transform.position.add(up.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 9f : 2f) * mouseY, new Vector3f()));
            Vector3f right = target.cross(up, new Vector3f());
            transform.position.add(right.mul(Time.deltaTime * (Input.isKeyDown(Input.Key.L_CONTROL) ? 9f : 2f) * mouseX));

            float scrollDelta = Input.getMouseScroll();
            zoom += scrollDelta;
            System.out.println(zoom);
        }
    }
}
