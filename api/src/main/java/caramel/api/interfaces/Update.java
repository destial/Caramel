package caramel.api.interfaces;

/**
 * Main interface for all updating classes.
 */
public interface Update {
    default void update() {}
    default void lateUpdate() {}
    default void editorUpdate() { update(); }
}
