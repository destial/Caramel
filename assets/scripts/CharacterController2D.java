package scripts;

import caramel.api.Time;
import caramel.api.interfaces.HideInEditor;
import caramel.api.interfaces.ShowInEditor;
import caramel.api.scripts.Script;
import caramel.api.objects.GameObject;
import caramel.api.Input;
import caramel.api.components.RigidBody2D;

public class CharacterController2D extends Script {
    @HideInEditor private transient RigidBody2D rb;
    @ShowInEditor public float jumpForce = 100f;

    public CharacterController2D(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        rb = getComponent(RigidBody2D.class);
    }

    @Override
    public void update() {
        if (rb == null || rb.rawBody == null) return;
        if (Input.isKeyDown(Input.Key.D)) {
            rb.setVelocity(5f, rb.getVelocity().y());
        } else if (Input.isKeyDown(Input.Key.A)) {
            rb.setVelocity(-5f, rb.getVelocity().y());
        }
        
        if (rb.isOnGround() && Input.isKeyDown(Input.Key.SPACE)) {
            rb.addVelocity(0, jumpForce * Time.deltaTime);
        }
    }
}