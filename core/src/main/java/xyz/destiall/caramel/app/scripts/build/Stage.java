package xyz.destiall.caramel.app.scripts.build;

public interface Stage {
    default boolean isReady() {
        return true;
    }
    Stage execute();
}
