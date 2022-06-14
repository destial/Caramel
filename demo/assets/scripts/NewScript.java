package scripts;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.api.debug.Debug;
import xyz.destiall.caramel.api.math.Vector2;
import xyz.destiall.caramel.api.math.Vector3;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.components.Camera;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.physics.components.Collider;
import xyz.destiall.caramel.api.physics.internals.Contactable2D;
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
    private transient boolean tp = false;

    @Override
    public void start() {
        spawnPos = new Vector3(transform.position);
        tp = false;
    }

    @Override
    public void update() {
        if (data.equalsIgnoreCase("player")) {
            RigidBody2D rb = getComponent(RigidBody2D.class);
            if (rb == null || rb.rawBody == null) return;
            rb.setVelocity(5f, rb.getVelocity().y());
            if (rb.isOnGround() && Input.isKeyDown(Input.Key.SPACE)) {
                rb.addVelocity(0, force * Time.deltaTime);
            }
            Camera camera = gameObject.scene.getGameCamera();
            if (camera == null) return;
            camera.transform.position.x = transform.position.x;
        }
    }

    @Override
    public void lateUpdate() {
        if (tp) {
            transform.setPosition(spawnPos.x(), spawnPos.y(), transform.position.z);
            Camera camera = gameObject.scene.getGameCamera();
            if (camera != null) {
                camera.transform.position.x = transform.position.x;
            }
            tp = false;
        }
    }

    @Override
    public void onCollisionEnter(RigidBody2D other) {
        if (other.gameObject.hasComponent(NewScript.class)) {
            String data = other.getComponent(NewScript.class).data;
            if (data.equalsIgnoreCase("spike")) {
                tp = true;
            }
        }
    }

    @Override
    public void onCollisionTrigger(Collider other) {
        if (other.gameObject.hasComponent(NewScript.class)) {
            String data = other.getComponent(NewScript.class).data;
            if (data.equalsIgnoreCase("win")) {
                tp = true;
            }
        }
    }
}