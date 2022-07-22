package caramel.api.render;

import caramel.api.Input;
import caramel.api.Time;
import caramel.api.components.Camera;
import caramel.api.debug.Debug;
import caramel.api.interfaces.HideInEditor;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.objects.GameObject;
import caramel.api.texture.Mesh;
import caramel.api.texture.MeshBuilder;
import caramel.api.utils.Color;
import imgui.ImGui;
import org.joml.Matrix3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class Button extends Renderer {
    @HideInEditor public Color currentColor = new Color(1f, 1f, 1f, 1f);
    @HideInEditor public Color targetColor = new Color(1f, 1f, 1f, 1f);

    public Mesh mesh;

    @InvokeOnEdit("updateColor") public Color hoverColor = new Color(1f, 0f, 0f, 1f);
    @InvokeOnEdit("updateColor") public Color clickColor = new Color(0f, 1f, 0f, 1f);
    @InvokeOnEdit("updateColor") public Color normalColor = new Color(1f, 1f, 1f, 1f);

    public transient boolean hovered = false;
    public transient boolean clicked = false;
    public transient final List<Runnable> onClick;
    public transient final List<Runnable> onHover;

    public float colorChangeTime = 1f;

    public Button(GameObject gameObject) {
        super(gameObject);
        onClick = new ArrayList<>();
        onHover = new ArrayList<>();
    }

    @Override
    public void build() {
        if (mesh != null) mesh.build();
    }

    public void updateColor() {
        if (hovered) {
            targetColor.set(hoverColor);
        } else if (clicked) {
            targetColor.set(clickColor);
        } else {
            targetColor.set(normalColor);
        }
        setColor();
    }

    public void setColor() {
        mesh.setColor(currentColor);
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
        setColor();
    }

    @Override
    public void render(Camera camera) {
        if (mesh == null) {
            mesh = MeshBuilder.createQuad(1);
            setColor();
            mesh.build();
        }

        if (mesh != null) {
            if (colorChangeTime <= 0) {
                currentColor.set(targetColor);
            } else {
                Color.lerp(targetColor, currentColor, Time.deltaTime * colorChangeTime);
            }
            setColor();
            mesh.render(transform, camera);
        }
    }

    @Override
    public void update() {
        boolean isHovering = isHovered();
        if (isHovering && (!hovered || clicked)) {
            targetColor.set(hoverColor);
            setColor();
            hovered = true;
            for (Runnable runnable : onHover) {
                runnable.run();
            }
            clicked = false;
        } else if (!isHovering && (hovered || clicked)) {
            targetColor.set(normalColor);
            setColor();
            hovered = false;
            clicked = false;
        }

        if (isHovering && ImGui.isMouseClicked(Input.Mouse.LEFT)) {
            targetColor.set(clickColor);
            setColor();
            clicked = true;
            for (Runnable runnable : onClick) {
                runnable.run();
            }
        }
    }

    public boolean isHovered() {
        float x = Input.getMouseWorldX();
        float y = Input.getMouseWorldY();

        Vector3f max = new Vector3f(0.5f, 0.5f, 1f);
        Vector3f min = new Vector3f(-0.5f, -0.5f, 1f);

        float tx = transform.position.x + transform.localPosition.x;
        float ty = transform.position.y + transform.localPosition.y;
        float tz = transform.position.z + transform.localPosition.z;

        Vector3f mX = max.mul(transform.model.get3x3(new Matrix3d())).add(tx, ty, tz);
        Vector3f mN = min.mul(transform.model.get3x3(new Matrix3d())).add(tx, ty, tz);

        return x >= mN.x && x <= mX.x && y >= mN.y && y <= mX.y;
    }

    @Override
    public void start() {}
}
