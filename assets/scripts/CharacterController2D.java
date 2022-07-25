package scripts;

import caramel.api.Time;
import caramel.api.interfaces.HideInEditor;
import caramel.api.interfaces.ShowInEditor;
import caramel.api.debug.Debug;
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
        float x = Input.getJoystickAxis(Input.Joystick.Axis.LEFT_X);
        float y = rb.getVelocity().y();
        if (Math.abs(x) > 0.1f) {
            x *= 5f;
        } else {
            x = rb.getVelocity().x();
        }

        if (Input.isKeyDown(Input.Key.D)) {
            x = 5f;
        } else if (Input.isKeyDown(Input.Key.A)) {
            x = -5f;
        }

	    rb.setVelocity(x, y);

        if (rb.isOnGround() && (Input.isJoystickPressed(Input.Joystick.Button.CIRCLE) || Input.isKeyDown(Input.Key.SPACE))) {
            rb.addVelocity(0, jumpForce * Time.deltaTime);
        }
    }

    @Override
    public void onCollisionEnter(RigidBody2D other) {

    }
}