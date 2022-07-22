package caramel.api.physics.components;

import caramel.api.components.RigidBody2D;
import caramel.api.physics.info.ContactPoint2D;

public interface Contactable2D {
    default void onCollisionEnter(RigidBody2D other) {}
    default void onCollisionExit(RigidBody2D other) {}
    default void onCollisionTrigger(Collider other) {}

    default void onCollisionEnterRaw(ContactPoint2D point2D) {}
}
