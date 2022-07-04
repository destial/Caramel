package xyz.destiall.caramel.api.render;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.interfaces.FunctionButton;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.texture.Spritesheet;

public final class SpriteRenderer extends Renderer {

    private final Vector3f pos;
    private final Quaternionf rot;
    private final Vector3f sca;

    public Spritesheet spritesheet;

    public SpriteRenderer(GameObject gameObject) {
        super(gameObject);
        pos = new Vector3f(transform.position);
        rot = new Quaternionf(transform.rotation);
        sca = new Vector3f(transform.scale);
    }

    @FunctionButton
    public void createMesh() {
        Debug.log("mesh boo");
    }

    @Override
    public void render(Camera camera) {
        if (spritesheet != null) spritesheet.render(transform, camera);
    }

    @Override
    public void start() {}

    @Override
    public void update() {}

    @Override
    public void lateUpdate() {
        transform.model
                .identity()
                .translate(pos.set(transform.position).add(transform.localPosition))
                .rotate(rot.set(transform.rotation).add(transform.localRotation))
                .scale(sca.set(transform.scale).mul(transform.localScale));
    }
}
