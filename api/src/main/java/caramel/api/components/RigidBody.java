package caramel.api.components;

import caramel.api.Component;
import caramel.api.objects.GameObject;
import caramel.api.physics.RigidBodyType;

public abstract class RigidBody extends Component {
    public float angularDamping = 0.8f;
    public float linearDamping = 0.9f;
    public float mass = 1f;
    public boolean fixedRotation = false;
    public boolean continuousCollision = false;
    public boolean isTrigger = false;

    public RigidBodyType bodyType = RigidBodyType.DYNAMIC;

    public RigidBody(GameObject gameObject) {
        super(gameObject);
    }

    public abstract void _setPosition(float x, float y, float z);
}
