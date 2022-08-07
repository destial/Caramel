package caramel.api.physics.components;

/**
 * Implement this if you want to register trigger events.
 */
public interface Contactable2D {
    default void onCollisionTrigger(Collider other) {}
}
