package scripts;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.joml.Vector3f;
import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.GameObject;
import xyz.destiall.caramel.api.Input;
import xyz.destiall.caramel.api.components.RigidBody2D;
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
        RigidBody2D rb = getComponent(RigidBody2D.class);
        if (rb == null) return;

        Debug.drawLine(other.transform.position, transform.position, new Vector3f(255, 0, 0));

        if (Input.isKeyPressed(Input.Key.SPACE)) {
            rb.rawBody.setLinearVelocity(new Vec2(rb.rawBody.m_linearVelocity.x, 5));
        }
        if (Input.isKeyDown(Input.Key.D)) {
            rb.rawBody.setLinearVelocity(new Vec2(-0.1f, rb.rawBody.m_linearVelocity.y));
        } else if (Input.isKeyDown(Input.Key.A)) {
            rb.rawBody.setLinearVelocity(new Vec2(0.1f, rb.rawBody.m_linearVelocity.y));
        }
    }
}