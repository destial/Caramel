package caramel.api.interfaces;

public interface Update {
    default void update() {}
    default void lateUpdate() {}
    default void editorUpdate() { update(); }
}
