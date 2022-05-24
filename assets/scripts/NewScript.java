package scripts;

import org.jbox2d.common.Vec2;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.physics.listeners.Contactable2D;
import xyz.destiall.caramel.interfaces.ShowInEditor;

public class NewScript extends Component implements Contactable2D {
    public NewScript(GameObject gameObject) {
        super(gameObject);
    }

    @ShowInEditor
    public float force = 100f;
    @ShowInEditor
    public String data = "floo";
    private transient Vector3f spawnPos;

    @Override
    public void start() {
        spawnPos = new Vector3f(transform.position);
    }

    @Override
    public void update() {
        if (data.equalsIgnoreCase("spike")) return;

        if (data.equalsIgnoreCase("player")) {
            RigidBody2D rb = getComponent(RigidBody2D.class);
            if (rb == null || rb.rawBody == null) return;
            if (Input.isKeyPressed(Input.Key.SPACE)) {
                rb.rawBody.applyForceToCenter(new Vec2(0, force));
            }
            if (Input.isKeyDown(Input.Key.A)) {
                rb.rawBody.applyForceToCenter(new Vec2(-1, 0));
            }
            if (Input.isKeyDown(Input.Key.D)) {
                rb.rawBody.applyForceToCenter(new Vec2(1, 0));
            }
            Camera camera = gameObject.scene.getGameCamera();
            if (camera == null) return;
            camera.transform.position.x = transform.position.x;
        }
    }

    @Override
    public void onCollisionEnter(RigidBody2D other) {
        if (other.gameObject.hasComponent(NewScript.class)) {
            String data = other.getComponent(NewScript.class).data;
            if (data.equalsIgnoreCase("spike")) {
                RigidBody2D rb = getComponent(RigidBody2D.class);
                if (rb == null || rb.rawBody == null) return;

                // rb.rawBody.applyForceToCenter(new Vec2(0, 250f));
            }
        }
    }
}