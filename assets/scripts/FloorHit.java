package scripts;

import caramel.api.objects.*;
import caramel.api.components.*;
import caramel.api.physics.components.*;
import caramel.api.interfaces.*;
import caramel.api.render.*;
import caramel.api.debug.*;
import caramel.api.audio.*;
import caramel.api.math.*;
import caramel.api.*;
import caramel.api.scripts.Script;

public class FloorHit extends Script {
    public transient AnimationUpdate animation;
    public FloorHit(GameObject gameObject) {
        super(gameObject);
    }

    // This method is called on the first frame
    @Override
    public void start() {
        GameObject player = findGameObject("Player");
        animation = player.getComponent(AnimationUpdate.class);
    }

    // This method is called on every frame
    @Override
    public void update() {
        
    }

    @Override
    public void onCollisionEnter(RigidBody2D other) {
        if (other.gameObject.name.equals("Floor")) {
            animation.score = 0;
            animation.button.getComponent(Text.class).text = "Hit " + animation.score;
            int test = 0;
        }
    }
}