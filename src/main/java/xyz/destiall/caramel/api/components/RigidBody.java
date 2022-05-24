package xyz.destiall.caramel.api.components;

import xyz.destiall.caramel.api.Component;
import xyz.destiall.caramel.api.objects.GameObject;
import xyz.destiall.caramel.api.physics.RigidBodyType;

public abstract class RigidBody extends Component {
    public float angularDamping = 0.8f;
    public float linearDamping = 0.9f;
    public float mass = 0f;
    public boolean fixedRotation = false;
    public boolean continuousCollision = true;

    public RigidBodyType bodyType = RigidBodyType.DYNAMIC;

    public RigidBody(GameObject gameObject) {
        super(gameObject);
    }
}
