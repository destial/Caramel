package scripts;

import caramel.api.objects.*;
import caramel.api.components.*;
import caramel.api.debug.*;
import caramel.api.audio.*;
import caramel.api.math.*;
import caramel.api.render.*;
import caramel.api.*;
import caramel.api.scripts.Script;

public class AnimationUpdate extends Script {
    public transient SpriteRenderer sp;
    public transient AudioPlayer ap;
    public RigidBody2D rb;
    public transient Button button;
    public int score = 0;
    public int maxScore = 0;
    public transient GameObject circle;
    public AnimationUpdate(GameObject gameObject) {
        super(gameObject);
    }

    // This method is called on the first frame
    @Override
    public void start() {
        sp = getComponent(SpriteRenderer.class);
        button = findGameObject("Button").getComponent(Button.class);
        circle = findGameObject("Circle");
        ap = getComponent(AudioPlayer.class);
        // rb = getComponent(RigidBody2D.class);
    }

    // This method is called on every frame
    @Override
    public void update() {
        if (rb == null || sp == null) return;
        Vector2 vel = rb.getVelocity();
        float circleY = circle.transform.position.y;
        float nowY = transform.position.y;
        float diffY = circleY - nowY;

        if (diffY > 0) {
            float circleX = circle.transform.position.x;
            float nowX = transform.position.x;
            float diffX = circleX - nowX;

            float y = rb.getVelocity().y();
            if (Math.abs(diffX) > 0.1f) {
                diffX *= 10f;
            } else {
                diffX = rb.getVelocity().x();
            }
            int multiplier = diffX < 0 ? -1 : 1;
            diffX = Math.min(Math.abs(diffX), 7.5f) * multiplier;
            rb.setVelocity(diffX, y);
            if (rb.isOnGround() && diffY < 2) {
                rb.addVelocity(0, 500f * Time.deltaTime);
            }
        }

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
        button.getComponentInChildren(Text.class).text = "Hit " + score + " (" + maxScore + ")";
    }

    @Override
    public void onCollisionEnter(RigidBody2D other) {
        if (other.gameObject.name.equals("Circle")) {
            score++;
            ap.play();
            if (score > maxScore) {
                maxScore = score;
            }
            int test = 2121122231;
        }
    }
}