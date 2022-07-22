package scripts;

import caramel.api.objects.*;
import caramel.api.components.*;
import caramel.api.debug.*;
import caramel.api.audio.*;
import caramel.api.math.*;
import caramel.api.render.*;
import caramel.api.scripts.Script;

public class AnimationUpdate extends Script {
    private transient SpriteRenderer sp;
    private transient RigidBody2D rb;
    public AnimationUpdate(GameObject gameObject) {
        super(gameObject);
    }

    // This method is called on the first frame
    @Override
    public void start() {
        sp = getComponent(SpriteRenderer.class);
        rb = getComponent(RigidBody2D.class);
    }

    // This method is called on every frame
    @Override
    public void update() {
        if (rb == null || sp == null) return;
        Vector2 vel = rb.getVelocity();
        if (vel.x() < 0 && !sp.animation.equals("anim3")) {
            sp.setAnimation("anim3");
        } else if (vel.x() > 0 && !sp.animation.equals("anim2")) {
            sp.setAnimation("anim2");
        }

        float x = Math.abs(vel.x());
        if (x >= 0 && x <= 1f && !sp.animation.equals("anim0")) {
            sp.setAnimation("anim0");
        }
        
    }
}