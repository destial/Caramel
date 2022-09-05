package caramel.api.physics.info;

import caramel.api.objects.GameObject;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.joml.Vector2f;

/**
 * Represents a ray-cast from a {@link GameObject}.
 */
public final class RaycastInfo2D implements RayCastCallback {
    private final GameObject requestingObject;
    public Fixture fixture;
    public Vector2f point;
    public Vector2f normal;
    public float fraction;
    public boolean hit;
    public GameObject hitObject;

    public RaycastInfo2D(final GameObject obj) {
        fixture = null;
        point = new Vector2f();
        normal = new Vector2f();
        fraction = 0.0f;
        hit = false;
        hitObject = null;
        this.requestingObject = obj;
    }

    @Override
    public float reportFixture(final Fixture fixture, final Vec2 point, final Vec2 normal, final float fraction) {
        if (fixture.m_userData == requestingObject) {
            return 1;
        }
        this.fixture = fixture;
        this.point = new Vector2f(point.x, point.y);
        this.normal = new Vector2f(normal.x, normal.y);
        this.fraction = fraction;
        this.hit = fraction != 0;
        this.hitObject = (GameObject)fixture.m_userData;

        return fraction;
    }
}
