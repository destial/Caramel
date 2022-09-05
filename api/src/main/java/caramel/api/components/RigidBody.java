package caramel.api.components;

import caramel.api.Component;
import caramel.api.interfaces.InvokeOnEdit;
import caramel.api.interfaces.ShowInEditor;
import caramel.api.objects.GameObject;
import caramel.api.physics.RigidBodyType;

/**
 * This {@link Component} is for physics dimension abstraction.
 */
public abstract class RigidBody extends Component {
    @InvokeOnEdit("updateBody") public float angularDamping = 0.8f;
    @InvokeOnEdit("updateBody") public float linearDamping = 0.9f;
    @InvokeOnEdit("updateBody") public float mass = 1f;
    @InvokeOnEdit("updateBody") public float friction = 1f;
    @InvokeOnEdit("updateBody") public boolean fixedRotation = false;
    @InvokeOnEdit("updateBody") public boolean continuousCollision = false;
    @InvokeOnEdit("updateBody") public boolean isTrigger = false;
    @InvokeOnEdit("updateBody") public boolean gravity = true;
    @ShowInEditor public boolean bounce = true;
    @InvokeOnEdit("updateBody") public RigidBodyType bodyType = RigidBodyType.DYNAMIC;


    public RigidBody(final GameObject gameObject) {
        super(gameObject);
    }

    public abstract void _setPosition(final float x, final float y, final float z);

    protected abstract void updateBody();
}
