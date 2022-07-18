package scripts;

import xyz.destiall.caramel.api.Time;
import xyz.destiall.caramel.api.scripts.Script;
import xyz.destiall.caramel.api.math.Vector3;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.physics.components.Collider;
import xyz.destiall.caramel.api.physics.components.Contactable2D;
import xyz.destiall.caramel.api.interfaces.*;
import xyz.destiall.caramel.api.debug.Debug;

public class Movement extends Script {
    public Movement(GameObject gameObject) {
        super(gameObject);
    }

    @ShowInEditor public float force = 100f;

    @Override
    public void start() {
        int x = 3;
    }

    @Override
    public void update() {
        RigidBody2D rb = getComponent(RigidBody2D.class);
        if (rb == null || rb.rawBody == null) return;
        if (Input.isKeyDown(Input.Key.D)) {
            rb.setVelocity(5f, rb.getVelocity().y());
        } else if (Input.isKeyDown(Input.Key.A)) {
            rb.setVelocity(-5f, rb.getVelocity().y());
        }
        
        if (rb.isOnGround() && Input.isKeyDown(Input.Key.SPACE)) {
            rb.addVelocity(0, force * Time.deltaTime);
        }
    }
}