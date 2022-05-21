package scripts;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.components.RigidBody3D;
import xyz.destiall.caramel.api.debug.Debug;

public class NewScript extends Component {
    public NewScript(GameObject gameObject) {
        super(gameObject);
    }

    private transient GameObject other;

    @Override
    public void start() {
        other = gameObject.scene.getGameObjects().stream().filter(g -> g != gameObject).findFirst().get();
    }

    @Override
    public void update() {
        RigidBody3D rb = getComponent(RigidBody3D.class);
        if (rb == null || rb.rawBody == null) return;

        Debug.log("gay lmao");

        Debug.drawLine(other.transform.position, transform.position, new Vector3f(255, 0, 0));

        if (Input.isKeyPressed(Input.Key.SPACE)) {
            rb.rawBody.setLinearVel(rb.rawBody.getLinearVel().get0(), 5, rb.rawBody.getLinearVel().get2());
        }
    }
}