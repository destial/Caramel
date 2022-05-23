package xyz.destiall.caramel.api.physics.listeners;

import xyz.destiall.caramel.api.components.RigidBody2D;
import xyz.destiall.caramel.api.physics.components.Collider;

public interface Contactable2D {
    default void onCollisionEnter(RigidBody2D other) {}
    default void onCollisionExit(RigidBody2D other) {}
    default void onCollisionTrigger(Collider other) {}
}
