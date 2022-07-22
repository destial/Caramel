package scripts;

import caramel.api.Time;
import caramel.api.scripts.Script;
import caramel.api.math.Vector3;
import caramel.api.objects.GameObject;
import caramel.api.Input;
import caramel.api.components.Camera;
import caramel.api.components.RigidBody2D;
import caramel.api.physics.components.Collider;
import caramel.api.physics.components.Contactable2D;
import caramel.api.interfaces.ShowInEditor;

public class NewScript extends Script implements Contactable2D {
    public NewScript(GameObject gameObject) {
        super(gameObject);
    }

    @ShowInEditor public float force = 100f;
    @ShowInEditor public String data = "floor";

    private transient Vector3 spawnPos;
    private transient boolean tp = false;

    @Override
    public void start() {
        spawnPos = new Vector3(transform.position);
        tp = false;
        int i = 0;
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