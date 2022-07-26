package scripts;

import caramel.api.objects.*;
import caramel.api.components.*;
import caramel.api.debug.*;
import caramel.api.audio.*;
import caramel.api.math.*;
import caramel.api.render.*;
import caramel.api.scripts.Script;

public class AnimationUpdate extends Script {
    public transient SpriteRenderer sp;
    public RigidBody2D rb;
    public transient Button button;
    public int score = 0;
    public AnimationUpdate(GameObject gameObject) {
        super(gameObject);
    }

    // This method is called on the first frame
    @Override
    public void start() {
        sp = getComponent(SpriteRenderer.class);
        button = findGameObject("Button").getComponent(Button.class);
    }

    // This method is called on every frame
    @Override
    public void update() {
        int i = 0;
        if (rb == null || sp == null) return;
        Vector2 vel = rb.getVelocity();
        float frameTime = 0.3f / Math.abs(vel.x());
        sp.timePerAnimation = frameTime;
        if (vel.x() < 0 && !sp.animation.equals("anim3")) {
            sp.setAnimation("anim3");
        } else if (vel.x() > 0 && !sp.animation.equals("anim2")) {
            sp.setAnimation("anim2");
        }
        float x = Math.abs(vel.x());
        if (x >= 0 && x <= 0.5f && (!sp.animation.equals("anim0") || !sp.animation.equals("anim1"))) {
            if (vel.x() < 0) sp.setAnimation("anim1");
            else sp.setAnimation("anim0");
        }
        button.getComponentInChildren(Text.class).text = "Hit " + score;
    }

    @Override
    public void onCollisionEnter(RigidBody2D other) {
        if (other.gameObject.name.equals("Circle")) {
            score++;
            int test = 0;
        }
    }
}