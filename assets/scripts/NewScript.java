package scripts;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.math.Vector2;
import xyz.destiall.caramel.api.math.Vector3;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.physics.listeners.Contactable2D;
import xyz.destiall.caramel.api.interfaces.ShowInEditor;

public class NewScript extends Component implements Contactable2D {
    public NewScript(GameObject gameObject) {
        super(gameObject);
    }

    @ShowInEditor
    public float force = 100f;
    @ShowInEditor
    public String data = "floor";
    private transient Vector3 spawnPos;

    @Override
    public void start() {
        spawnPos = new Vector3(transform.position);
    }

    @Override
    public void update() {
        if (data.equalsIgnoreCase("spike")) return;

        if (data.equalsIgnoreCase("player")) {
            RigidBody2D rb = getComponent(RigidBody2D.class);
            if (rb == null || rb.rawBody == null) return;
            if (Input.isKeyPressed(Input.Key.SPACE)) {
                rb.addForce(new Vector2(0, force * 3));
            }
            if (Input.isKeyDown(Input.Key.A)) {
                rb.addForce(new Vector2(-force / 10, 0));
            }
            if (Input.isKeyDown(Input.Key.D)) {
                rb.addForce(new Vector2(force / 10, 0));
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